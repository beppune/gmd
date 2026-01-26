 
/* ===================================================================== 
 MAGAZZINO -> GMD : MIGRAZIONI OPERATIVE (OGGI) + CLEANUP 
 INCLUDE: 
 1) ORDERS (magazzino.documenti + deduzione da magazzino.transazioni) 
 2) ORDERS_LINES (magazzino.transazioni) [SN/PT unici per ordine] 
 3) TRANSACTIONS LOG (magazzino.transazioni -> gmd.transactions) [INBOUND/OUTBOUND] 
 4) STORAGE (GIACENZE)(magazzino.giacenze -> gmd.storage) 
 6) PERMISSIONS (operator, dc) 
 ESCLUDE: 
 - Anagrafiche (dcs/locations/items/suppliers/operators) 
 - Spedizioni (shippings) 
 PREREQUISITI (già risolti): 
 - gmd.dcs, gmd.locations, gmd.items, gmd.suppliers, gmd.operators popolati 
 - Tabelle di mapping presenti (verranno droppate in cleanup): 
 gmd.items_map(old_desc->new_name) 
 gmd.user_map(old_userid->uid_final) 
 gmd.locations_stage(dc, old_pos, final15) 
 COMPATIBILITA': MySQL 8+ (ROW_NUMBER, JSON_VALID/EXTRACT) 
 ===================================================================== */ 
SET sql_safe_updates = 0; 

/* ===================================================================== 
 1) ORDERS 
 ===================================================================== */ 
CREATE TABLE IF NOT EXISTS gmd.orders_stage ( 
 doc_interno VARCHAR(64) PRIMARY KEY, 
 issued DATETIME NOT NULL, 
 operator CHAR(8) NULL, 
 datacenter CHAR(4) NULL, 
 supplier VARCHAR(200) NULL, 
 type ENUM('INBOUND','OUTBOUND') NULL, 
 subject ENUM('SUPPLIER','SUPPLIER_DC','INTERNAL') NULL, 
 status ENUM('PENDING','COMPLETED','CANCELED') NOT NULL, 
 ref_value VARCHAR(100) NOT NULL, 
 remarks VARCHAR(1000) NULL 
); 
CREATE TABLE IF NOT EXISTS gmd.orders_audit ( 
 issue_type VARCHAR(50) NOT NULL, 
 doc_interno VARCHAR(64) NOT NULL, 
 details VARCHAR(500) NULL, 
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP 
); 
TRUNCATE TABLE gmd.orders_stage; 
TRUNCATE TABLE gmd.orders_audit; 
INSERT INTO gmd.orders_stage( 
 doc_interno, issued, operator, datacenter, supplier, type, subject, status, ref_value, remarks 
) 
SELECT 
 d.NumeroDocInterno AS doc_interno, 
 TIMESTAMP(d.DataDocumento) AS issued, 
 tx.operator_uid AS operator, 
 tx.dc_abbr AS datacenter, 
 COALESCE(df.NomeFornitore, dm.Trasportatore, 'POSTE ITALIANE S.P.A.') AS supplier, 
 tx.order_type AS type, 
 CASE 
 WHEN df.NumeroDocInterno IS NOT NULL THEN 'SUPPLIER' 
 WHEN dm.NumeroDoc IS NOT NULL THEN 'SUPPLIER_DC' 
 ELSE 'INTERNAL' 
 END AS subject, 
 'COMPLETED' AS status, 
 COALESCE( 
 NULLIF( 
 CASE WHEN d.Note IS NOT NULL AND JSON_VALID(d.Note) 
 THEN JSON_UNQUOTE(JSON_EXTRACT(d.Note,'$.REP')) 
 ELSE NULL 
 END, 
 '' 
 ), 
 NULLIF(CONCAT(u_src.Cognome, ' ', u_src.Nome), ' '), 
 NULLIF(CONCAT(o.lastname, ' ', o.firstname), ' '), 
 NULLIF(tx.user_oldid, ''), 
 'UNKNOWN' 
 ) AS ref_value, 
 LEFT( 
 CONCAT( 
 'DOC=', d.NumeroDocInterno, 
 ' \n ', 
 CASE 
 WHEN d.Note IS NULL THEN '' 
 WHEN JSON_VALID(d.Note) THEN CONCAT_WS(' \n ', 
 NULLIF(JSON_UNQUOTE(JSON_EXTRACT(d.Note,'$.REMARKS')), ''), 
 IFNULL(CONCAT('PROJECT=', NULLIF(JSON_UNQUOTE(JSON_EXTRACT(d.Note,'$.PROJECT')), '')), NULL), 
 IFNULL(CONCAT('MOTIVE=', NULLIF(JSON_UNQUOTE(JSON_EXTRACT(d.Note,'$.MOTIVE')), '')), NULL), 
 IFNULL(CONCAT('HAULER=', NULLIF(JSON_UNQUOTE(JSON_EXTRACT(d.Note,'$.HAULER')), '')), NULL), 
 IFNULL(CONCAT('REP=', NULLIF(JSON_UNQUOTE(JSON_EXTRACT(d.Note,'$.REP')), '')), NULL) 
 ) 
 ELSE d.Note 
 END 
 ), 
 1000 
 ) AS remarks 
FROM magazzino.documenti d 
LEFT JOIN magazzino.documentifornitori df 
 ON df.NumeroDocInterno = d.NumeroDocInterno 
LEFT JOIN magazzino.documentazionemoving dm 
 ON dm.NumeroDoc = d.NumeroDocInterno 
LEFT JOIN ( 
 SELECT 
 t.NumeroDocInterno, 
 SUBSTRING_INDEX(GROUP_CONCAT(t.UserID ORDER BY t.Data SEPARATOR ','), ',', 1) AS user_oldid, 
 SUBSTRING_INDEX(GROUP_CONCAT(um.uid_final ORDER BY t.Data SEPARATOR ','), ',', 1) AS operator_uid, 
 SUBSTRING_INDEX(GROUP_CONCAT(dc.Abbreviazione ORDER BY t.Data SEPARATOR ','), ',', 1) AS dc_abbr, 
 CASE 
 WHEN SUM(t.tipoOperazione='CARICO') > 0 AND SUM(t.tipoOperazione='SCARICO') = 0 THEN 'INBOUND' 
 WHEN SUM(t.tipoOperazione='SCARICO') > 0 AND SUM(t.tipoOperazione='CARICO') = 0 THEN 'OUTBOUND' 
 WHEN SUM(t.tipoOperazione='SCARICO') > 0 THEN 'OUTBOUND' 
 ELSE 'INBOUND' 
 END AS order_type 
 FROM magazzino.transazioni t 
 LEFT JOIN gmd.user_map um 
 ON um.old_userid = t.UserID 
 LEFT JOIN magazzino.datacenters dc 
 ON dc.NomeDc = t.NomeDc 
 GROUP BY t.NumeroDocInterno 
) tx 
 ON tx.NumeroDocInterno = d.NumeroDocInterno 
LEFT JOIN gmd.operators o 
 ON o.uid = tx.operator_uid 
LEFT JOIN magazzino.utenti u_src 
 ON u_src.UserID = tx.user_oldid; 

-- Audit utile 
INSERT INTO gmd.orders_audit(issue_type, doc_interno, details) 
SELECT 'REF_UNKNOWN', s.doc_interno, 'ref_value=UNKNOWN (nessun fallback disponibile)' 
FROM gmd.orders_stage s 
WHERE s.ref_value = 'UNKNOWN'; 

-- Inserimento ORDERS: evita duplicati in rerun tramite DOC=... 
INSERT INTO gmd.orders(operator, datacenter, supplier, issued, type, subject, status, ref, remarks) 
SELECT 
 s.operator, 
 s.datacenter, 
 s.supplier, 
 s.issued, 
 s.type, 
 s.subject, 
 'COMPLETED' AS status, 
 s.ref_value, 
 s.remarks 
FROM gmd.orders_stage s 
JOIN gmd.operators o ON o.uid = s.operator 
JOIN gmd.dcs d ON d.shortname = s.datacenter 
JOIN gmd.suppliers p ON p.name = s.supplier 
WHERE NOT EXISTS ( 
 SELECT 1 
 FROM gmd.orders ox 
 WHERE ox.remarks LIKE CONCAT('DOC=', s.doc_interno, '%') 
); 

-- Forza lo stato COMPLETED anche per ordini già presenti (rerun)
UPDATE gmd.orders
SET status = 'COMPLETED'
WHERE remarks LIKE 'DOC=%';

/* ===================================================================== 
 2) ORDERS_LINES (ownedby deve puntare a gmd.orders.id) 
 ===================================================================== */ 
CREATE TABLE IF NOT EXISTS gmd.order_doc_map ( 
 ownedby INT PRIMARY KEY, 
 doc_interno VARCHAR(64) NOT NULL, 
 KEY(doc_interno) 
); 
TRUNCATE TABLE gmd.order_doc_map; 
INSERT INTO gmd.order_doc_map(ownedby, doc_interno) 
SELECT 
 o.id AS ownedby, 
 TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(o.remarks, 'DOC=', -1), ' \n', 1)) AS doc_interno 
FROM gmd.orders o 
WHERE o.remarks LIKE 'DOC=%'; 

CREATE TABLE IF NOT EXISTS gmd.orders_lines_stage ( 
 src_id BIGINT NOT NULL, 
 ownedby INT NULL, 
 datacenter CHAR(8) NULL, 
 item VARCHAR(250) NULL, 
 pos VARCHAR(15) NULL, 
 amount INT NOT NULL, 
 sn_final VARCHAR(100) NULL, 
 pt_final VARCHAR(100) NULL, 
 PRIMARY KEY (src_id) 
); 
CREATE TABLE IF NOT EXISTS gmd.orders_lines_audit ( 
 issue_type VARCHAR(50) NOT NULL, 
 doc_interno VARCHAR(64) NOT NULL, 
 src_id BIGINT NULL, 
 details VARCHAR(500) NULL, 
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP 
); 
TRUNCATE TABLE gmd.orders_lines_stage; 
TRUNCATE TABLE gmd.orders_lines_audit; 

INSERT INTO gmd.orders_lines_stage(src_id, ownedby, datacenter, item, pos, amount, sn_final, pt_final) 
SELECT 
 t.ID AS src_id, 
 odm.ownedby AS ownedby, 
 COALESCE(dc.Abbreviazione, t.NomeDc) AS datacenter, 
 COALESCE(im.new_name, t.DescrizioneMerce) AS item, 
 COALESCE(ls.final15, 
 LEFT(REPLACE(REPLACE(REPLACE(TRIM(t.Posizione), ' ', ''), '–', '-'), '—', '-'), 15) 
 ) AS pos, 
 t.Quantita AS amount, 
 CASE 
 WHEN NULLIF(t.Seriale,'') IS NULL THEN NULL 
 WHEN ROW_NUMBER() OVER (PARTITION BY odm.ownedby, NULLIF(t.Seriale,'') ORDER BY t.ID) = 1 
 THEN NULLIF(t.Seriale,'') 
 ELSE NULL 
 END AS sn_final, 
 CASE 
 WHEN NULLIF(t.PtNumber,'') IS NULL THEN NULL 
 WHEN ROW_NUMBER() OVER (PARTITION BY odm.ownedby, NULLIF(t.PtNumber,'') ORDER BY t.ID) = 1 
 THEN NULLIF(t.PtNumber,'') 
 ELSE NULL 
 END AS pt_final 
FROM magazzino.transazioni t 
LEFT JOIN gmd.order_doc_map odm 
 ON odm.doc_interno = t.NumeroDocInterno 
LEFT JOIN magazzino.datacenters dc 
 ON dc.NomeDc = t.NomeDc 
LEFT JOIN gmd.items_map im 
 ON im.old_desc = t.DescrizioneMerce 
LEFT JOIN gmd.locations_stage ls 
 ON ls.dc = COALESCE(dc.Abbreviazione, t.NomeDc) 
 AND ls.old_pos = t.Posizione 
WHERE t.Quantita > 0; 

-- Audit righe senza ownedby (versione robusta: doc_interno da transazione sorgente con fallback)
INSERT INTO gmd.orders_lines_audit(issue_type, doc_interno, src_id, details)
SELECT
  'ORDER_NOT_FOUND' AS issue_type,
  COALESCE(t.NumeroDocInterno, 'UNKNOWN_DOC') AS doc_interno,
  s.src_id,
  'Nessun ordine trovato per DOC=<NumeroDocInterno>' AS details
FROM gmd.orders_lines_stage s
LEFT JOIN magazzino.transazioni t
  ON t.ID = s.src_id
WHERE s.ownedby IS NULL;

-- Inserimento finale (ownedby obbligatorio) 
INSERT INTO gmd.orders_lines(ownedby, datacenter, item, pos, amount, sn, pt) 
SELECT 
 s.ownedby, 
 s.datacenter, 
 s.item, 
 s.pos, 
 s.amount, 
 s.sn_final, 
 s.pt_final 
FROM gmd.orders_lines_stage s 
JOIN gmd.orders o ON o.id = s.ownedby 
JOIN gmd.items it ON it.name = s.item 
JOIN gmd.locations l ON l.dc = s.datacenter AND l.name = s.pos 
WHERE s.ownedby IS NOT NULL; 

/* ===================================================================== 
 3) TRANSACTIONS (LOG) - INBOUND/OUTBOUND 
 ===================================================================== */ 
-- (Opzionale) per rerun pulito: 
-- TRUNCATE TABLE gmd.transactions; 
INSERT INTO gmd.transactions(operator, type, `timestamp`, item, dc, pos, amount, sn, pt) 
SELECT 
 COALESCE(um.uid_final, t.UserID) AS operator, 
 CASE 
 WHEN t.tipoOperazione='CARICO' THEN 'INBOUND' 
 WHEN t.tipoOperazione='SCARICO' THEN 'OUTBOUND' 
 ELSE NULL 
 END AS type, 
 CAST(t.Data AS DATETIME) AS `timestamp`, 
 COALESCE(im.new_name, t.DescrizioneMerce) AS item, 
 COALESCE(dc.Abbreviazione, t.NomeDc) AS dc, 
 t.Posizione AS pos, 
 t.Quantita AS amount, 
 NULLIF(t.Seriale,'') AS sn, 
 NULLIF(t.PtNumber,'') AS pt 
FROM magazzino.transazioni t 
LEFT JOIN gmd.user_map um ON um.old_userid = t.UserID 
LEFT JOIN gmd.items_map im ON im.old_desc = t.DescrizioneMerce 
LEFT JOIN magazzino.datacenters dc ON dc.NomeDc = t.NomeDc 
WHERE t.Quantita > 0 AND t.tipoOperazione IN ('CARICO','SCARICO'); 

/* ===================================================================== 
 4) STORAGE (GIACENZE) 
 ===================================================================== */ 
CREATE TABLE IF NOT EXISTS gmd.storage_stage_raw ( 
 dc CHAR(4), 
 pos VARCHAR(15), 
 item VARCHAR(250), 
 amount INT, 
 sn VARCHAR(250), 
 pt VARCHAR(12) 
); 
CREATE TABLE IF NOT EXISTS gmd.storage_stage_agg ( 
 item VARCHAR(250) NOT NULL, 
 dc CHAR(4) NOT NULL, 
 pos VARCHAR(15) NOT NULL, 
 amount INT NOT NULL, 
 sn_candidate VARCHAR(250) NULL, 
 pt_candidate VARCHAR(12) NULL 
); 
CREATE TABLE IF NOT EXISTS gmd.storage_stage_final ( 
 item VARCHAR(250) NOT NULL, 
 dc CHAR(4) NOT NULL, 
 pos VARCHAR(15) NOT NULL, 
 amount INT NOT NULL, 
 sn_final VARCHAR(250) NULL, 
 pt_final VARCHAR(12) NULL, 
 PRIMARY KEY(item, dc, pos) 
); 
CREATE TABLE IF NOT EXISTS gmd.storage_audit ( 
 issue_type VARCHAR(60) NOT NULL, 
 dc VARCHAR(20) NULL, 
 pos VARCHAR(32) NULL, 
 item VARCHAR(256) NULL, 
 details VARCHAR(500) NULL, 
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP 
); 
TRUNCATE TABLE gmd.storage_stage_raw; 
TRUNCATE TABLE gmd.storage_stage_agg; 
TRUNCATE TABLE gmd.storage_stage_final; 
TRUNCATE TABLE gmd.storage_audit; 
INSERT INTO gmd.storage_stage_raw(dc, pos, item, amount, sn, pt) 
SELECT 
 dcsrc.Abbreviazione AS dc, 
 COALESCE(ls.final15, 
 LEFT(REPLACE(REPLACE(REPLACE(TRIM(g.Posizione), ' ', ''), '–', '-'), '—', '-'), 15) 
 ) AS pos, 
 COALESCE(im.new_name, LEFT(TRIM(g.DescrizioneMerce), 250)) AS item, 
 g.Quantita AS amount, 
 NULLIF(g.Seriale,'') AS sn, 
 NULLIF(g.PtNumber,'') AS pt 
FROM magazzino.giacenze g 
LEFT JOIN magazzino.datacenters dcsrc ON dcsrc.NomeDc = g.NomeDc 
LEFT JOIN gmd.items_map im ON im.old_desc = g.DescrizioneMerce 
LEFT JOIN gmd.locations_stage ls ON ls.dc = dcsrc.Abbreviazione AND ls.old_pos = g.Posizione 
WHERE g.Quantita > 0; 
INSERT INTO gmd.storage_stage_agg(item, dc, pos, amount, sn_candidate, pt_candidate) 
SELECT 
 r.item, r.dc, r.pos, 
 SUM(r.amount) AS amount, 
 CASE WHEN COUNT(DISTINCT CASE WHEN r.sn IS NOT NULL THEN r.sn END)=1 THEN MAX(r.sn) ELSE NULL END, 
 CASE WHEN COUNT(DISTINCT CASE WHEN r.pt IS NOT NULL THEN r.pt END)=1 THEN MAX(r.pt) ELSE NULL END 
FROM gmd.storage_stage_raw r 
WHERE r.dc IS NOT NULL AND r.pos IS NOT NULL AND r.item IS NOT NULL 
GROUP BY r.item, r.dc, r.pos; 
-- Conflitti intra-gruppo SN/PT 
INSERT INTO gmd.storage_audit(issue_type, dc, pos, item, details) 
SELECT 'SNPT_CONFLICT_IN_GROUP', a.dc, a.pos, a.item, 
 'Più SN/PT per lo stesso (item,dc,pos): sn/pt impostati a NULL' 
FROM ( 
 SELECT 
 item, dc, pos, 
 COUNT(DISTINCT CASE WHEN sn IS NOT NULL THEN sn END) AS sn_cnt, 
 COUNT(DISTINCT CASE WHEN pt IS NOT NULL THEN pt END) AS pt_cnt 
 FROM gmd.storage_stage_raw 
 GROUP BY item, dc, pos 
) a 
WHERE a.sn_cnt > 1 OR a.pt_cnt > 1; 
-- Unicità globale su storage.sn / storage.pt 
INSERT INTO gmd.storage_stage_final(item, dc, pos, amount, sn_final, pt_final) 
SELECT 
 a.item, a.dc, a.pos, a.amount, 
 CASE WHEN a.sn_candidate IS NULL THEN NULL 
 WHEN ROW_NUMBER() OVER (PARTITION BY a.sn_candidate ORDER BY a.dc, a.pos, a.item)=1 THEN a.sn_candidate 
 ELSE NULL END, 
 CASE WHEN a.pt_candidate IS NULL THEN NULL 
 WHEN ROW_NUMBER() OVER (PARTITION BY a.pt_candidate ORDER BY a.dc, a.pos, a.item)=1 THEN a.pt_candidate 
 ELSE NULL END 
FROM gmd.storage_stage_agg a 
JOIN gmd.items i ON i.name = a.item 
JOIN gmd.locations l ON l.dc = a.dc AND l.name = a.pos 
WHERE a.amount > 0; 
INSERT INTO gmd.storage(item, dc, pos, amount, sn, pt) 
SELECT item, dc, pos, amount, sn_final, pt_final 
FROM gmd.storage_stage_final 
ON DUPLICATE KEY UPDATE amount=VALUES(amount), sn=VALUES(sn), pt=VALUES(pt); 

/* =====================================================================
 6) PERMISSIONS (operator, dc)
 Derivate da magazzino.utenti.AreaAppartenenza e magazzino.datacenters
 Vincolo operativo: solo DC con shortname in ('TOT','TOR','MGZ','EUR','DGT')
 ===================================================================== */

-- Mantieni/crea solo permessi sui DC operativi
INSERT INTO gmd.permissions (operator, dc)
SELECT DISTINCT
  COALESCE(um.uid_final, u.UserID) AS operator,
  dc.Abbreviazione AS dc
FROM magazzino.utenti u
LEFT JOIN gmd.user_map um
  ON um.old_userid = u.UserID
JOIN magazzino.datacenters dc
  ON dc.AreaAppartenenza = u.AreaAppartenenza
WHERE u.AreaAppartenenza IS NOT NULL
  AND u.AreaAppartenenza <> ''
  AND dc.Abbreviazione IN ('TOT','TOR','MGZ','EUR','DGT')
  AND EXISTS (
    SELECT 1 FROM gmd.operators o
    WHERE o.uid = COALESCE(um.uid_final, u.UserID)
  )
  AND EXISTS (
    SELECT 1 FROM gmd.dcs d
    WHERE d.shortname = dc.Abbreviazione
  )
ON DUPLICATE KEY UPDATE operator = operator;

-- DELETE prudente: rimuove permessi su DC non operativi SOLO per operatori derivati da magazzino.utenti (perimetro migrazione)
DELETE p
FROM gmd.permissions p
WHERE p.dc NOT IN ('TOT','TOR','MGZ','EUR','DGT')
  AND p.operator IN (
    SELECT DISTINCT
      COALESCE(um.uid_final, u.UserID) AS operator
    FROM magazzino.utenti u
    LEFT JOIN gmd.user_map um
      ON um.old_userid = u.UserID
    WHERE u.AreaAppartenenza IS NOT NULL
      AND u.AreaAppartenenza <> ''
      AND EXISTS (
        SELECT 1 FROM gmd.operators o
        WHERE o.uid = COALESCE(um.uid_final, u.UserID)
      )
  );

/* =====================================================================
 7) CHECK A: tutti gli ordini devono essere COMPLETED
  - atteso: not_completed = 0
 ===================================================================== */
SELECT COUNT(*) AS not_completed
FROM gmd.orders
WHERE status <> 'COMPLETED';

/* ===================================================================== 
 8) CLEANUP (come richiesto) 
 - Elimina stage/audit/temp e anche le map.
 - Elimina anche items_stage e users_stage (se presenti)
 ===================================================================== */ 
DROP TABLE IF EXISTS 
 gmd.orders_stage, 
 gmd.orders_audit, 
 gmd.order_doc_map, 
 gmd.orders_lines_stage, 
 gmd.orders_lines_audit, 
 gmd.storage_stage_raw, 
 gmd.storage_stage_agg, 
 gmd.storage_stage_final, 
 gmd.storage_audit, 
 gmd.items_stage,
 gmd.users_stage; 
DROP TABLE IF EXISTS 
 gmd.items_map, 
 gmd.user_map, 
 gmd.locations_stage, 
 gmd.suppliers_stage; 
SET sql_safe_updates = 1;