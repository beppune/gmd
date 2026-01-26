/* ============================================================
   MIGRAZIONE MAGA -> GMD (ANAGRAFICHE)
   ESCLUDE: GIACENZE / STORAGE
   Compatibilità: MySQL 8+ (usa window functions) ma NON usa CTE (WITH)
   Schemi: maga (source), gmd (target)
   ============================================================ */

SET sql_safe_updates = 0;

/* 1) DATACENTER -> gmd.dcs */
INSERT INTO gmd.dcs(shortname, fullname, legal)
SELECT d.Abbreviazione, d.NomeDc, 'LEGAL'
FROM magazzino.datacenters d
ON DUPLICATE KEY UPDATE
  fullname = VALUES(fullname),
  legal    = VALUES(legal);

/* 2) LOCATIONS -> gmd.locations (no CTE) */
CREATE TABLE IF NOT EXISTS gmd.locations_stage (
  dc CHAR(4) NOT NULL,
  old_pos VARCHAR(32) NOT NULL,
  base15 VARCHAR(15) NOT NULL,
  rn INT NOT NULL,
  final15 VARCHAR(15) NOT NULL,
  PRIMARY KEY (dc, old_pos)
);

TRUNCATE TABLE gmd.locations_stage;

INSERT INTO gmd.locations_stage(dc, old_pos, base15, rn, final15)
SELECT
  dc,
  old_pos,
  base15,
  rn,
  CASE
    WHEN rn = 1 THEN base15
    ELSE CONCAT(LEFT(base15, 12), '-', LPAD(rn, 2, '0'))
  END AS final15
FROM (
  SELECT
    dc,
    old_pos,
    base15,
    ROW_NUMBER() OVER (PARTITION BY dc, base15 ORDER BY old_pos) AS rn
  FROM (
    SELECT
      d.Abbreviazione AS dc,
      p.Posizione     AS old_pos,
      LEFT(
        REPLACE(REPLACE(REPLACE(TRIM(p.Posizione), ' ', ''), '–', '-'), '—', '-'),
        15
      ) AS base15
    FROM magazzino.posizioni p
    JOIN magazzino.datacenters d
      ON d.NomeDc = p.NomeDc
  ) norm
) ranked;

INSERT IGNORE INTO gmd.locations(dc, name)
SELECT dc, final15
FROM gmd.locations_stage;

/* 3) FORNITORI -> gmd.suppliers (opzione 2, no CTE) */
CREATE TABLE IF NOT EXISTS gmd.suppliers_stage (
  name  VARCHAR(200) PRIMARY KEY,
  legal VARCHAR(256) NOT NULL,
  piva  CHAR(11) NOT NULL,
  piva_is_placeholder TINYINT(1) NOT NULL DEFAULT 0
);

TRUNCATE TABLE gmd.suppliers_stage;

INSERT INTO gmd.suppliers_stage(name, legal, piva, piva_is_placeholder)
SELECT
  name,
  legal,
  CASE
    WHEN is_ph = 0 THEN piva_pre
    WHEN rn = 1 THEN piva_pre
    ELSE LPAD(MOD(CRC32(CONCAT(name, '#', rn)), 100000000000), 11, '0')
  END AS piva,
  is_ph
FROM (
  SELECT
    name,
    legal,
    piva_pre,
    is_ph,
    ROW_NUMBER() OVER (PARTITION BY piva_pre, is_ph ORDER BY name) AS rn
  FROM (
    SELECT
      np.Nome AS name,
      'LEGAL' AS legal,
      CASE
        WHEN p.PartitaIva IS NOT NULL AND p.PartitaIva <> '' THEN p.PartitaIva
        ELSE LPAD(MOD(CRC32(np.Nome), 100000000000), 11, '0')
      END AS piva_pre,
      CASE
        WHEN p.PartitaIva IS NOT NULL AND p.PartitaIva <> '' THEN 0
        ELSE 1
      END AS is_ph
    FROM magazzino.nomepartners np
    LEFT JOIN magazzino.partners p
      ON p.Nome = np.Nome
  ) prepared
) ranked;

INSERT INTO gmd.suppliers(name, legal, piva)
SELECT name, legal, piva
FROM gmd.suppliers_stage
ON DUPLICATE KEY UPDATE
  legal = VALUES(legal),
  piva  = VALUES(piva);

/* 4) INDIRIZZI FORNITORI -> gmd.suppliers_addresses */
INSERT IGNORE INTO gmd.suppliers_addresses(supplier, address)
SELECT s.NomePartner, s.Indirizzo
FROM magazzino.sedi s
JOIN gmd.suppliers sup
  ON sup.name = s.NomePartner;

/* 5) MERCI -> gmd.items (+ items_map), no CTE */
CREATE TABLE IF NOT EXISTS gmd.items_stage (
  old_desc VARCHAR(256) PRIMARY KEY,
  base250  VARCHAR(250) NOT NULL,
  rn       INT NOT NULL,
  final_name VARCHAR(250) NOT NULL,
  was_truncated TINYINT(1) NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS gmd.items_map (
  old_desc VARCHAR(256) PRIMARY KEY,
  new_name VARCHAR(250) NOT NULL,
  was_truncated TINYINT(1) NOT NULL DEFAULT 0
);

TRUNCATE TABLE gmd.items_stage;

INSERT INTO gmd.items_stage(old_desc, base250, rn, final_name, was_truncated)
SELECT
  old_desc,
  base250,
  rn,
  CASE
    WHEN rn = 1 THEN base250
    ELSE CONCAT(LEFT(base250, 245), '~', LPAD(rn, 4, '0'))
  END AS final_name,
  was_truncated
FROM (
  SELECT
    old_desc,
    base250,
    was_truncated,
    ROW_NUMBER() OVER (PARTITION BY base250 ORDER BY old_desc) AS rn
  FROM (
    SELECT
      m.Descrizione AS old_desc,
      LEFT(TRIM(m.Descrizione), 250) AS base250,
      CASE WHEN CHAR_LENGTH(m.Descrizione) > 250 THEN 1 ELSE 0 END AS was_truncated
    FROM magazzino.merci m
  ) norm
) ranked;

INSERT IGNORE INTO gmd.items(name)
SELECT final_name
FROM gmd.items_stage;

TRUNCATE TABLE gmd.items_map;

INSERT INTO gmd.items_map(old_desc, new_name, was_truncated)
SELECT old_desc, final_name, was_truncated
FROM gmd.items_stage;

/* 6) UTENTI -> gmd.operators (+ user_map), no CTE */
CREATE TABLE IF NOT EXISTS gmd.users_stage (
  old_userid VARCHAR(16) PRIMARY KEY,
  uid_base   CHAR(8) NOT NULL,
  rn         INT NOT NULL,
  uid_final  CHAR(8) NOT NULL,
  firstname  VARCHAR(100) NOT NULL,
  lastname   VARCHAR(100) NOT NULL,
  email      VARCHAR(256) NOT NULL,
  role       ENUM('OPERATOR','ADMIN') NOT NULL,
  active     TINYINT(1) NOT NULL,
  localpassword VARCHAR(50) NULL
);

CREATE TABLE IF NOT EXISTS gmd.user_map (
  old_userid VARCHAR(16) PRIMARY KEY,
  uid_final  CHAR(8) NOT NULL,
  email      VARCHAR(256) NOT NULL
);

TRUNCATE TABLE gmd.users_stage;

INSERT INTO gmd.users_stage(old_userid, uid_base, rn, uid_final, firstname, lastname, email, role, active, localpassword)
SELECT
  old_userid,
  uid_base,
  rn,
  CASE
    WHEN rn = 1 THEN uid_base
    ELSE CONCAT(LEFT(uid_base, 6), LPAD(rn, 2, '0'))
  END AS uid_final,
  firstname,
  lastname,
  email,
  role,
  active,
  NULL AS localpassword
FROM (
  SELECT
    old_userid,
    uid_base,
    firstname,
    lastname,
    email,
    role,
    active,
    ROW_NUMBER() OVER (PARTITION BY uid_base ORDER BY old_userid) AS rn
  FROM (
    SELECT
      u.UserID AS old_userid,
      UPPER(LEFT(u.UserID, 8)) AS uid_base,
      COALESCE(NULLIF(u.Nome,''), 'UNKNOWN') AS firstname,
      COALESCE(NULLIF(u.Cognome,''), 'UNKNOWN') AS lastname,
      u.Email AS email,
      CASE WHEN u.Permesso = 'AMMINISTRATORE' THEN 'ADMIN' ELSE 'OPERATOR' END AS role,
      CASE WHEN u.Permesso = 'DISABILITATO' THEN 0 ELSE 1 END AS active
    FROM magazzino.utenti u
  ) norm
) ranked;

/* (Opzionale) localpassword da MAGA
UPDATE gmd.users_stage s
JOIN magazzino.operators o ON o.op = s.uid_final
SET s.localpassword = o.passwd;
*/

INSERT INTO gmd.operators(uid, lastname, firstname, email, role, active, localpassword)
SELECT uid_final, lastname, firstname, email, role, active, localpassword
FROM gmd.users_stage
ON DUPLICATE KEY UPDATE
  lastname = VALUES(lastname),
  firstname = VALUES(firstname),
  role = VALUES(role),
  active = VALUES(active),
  localpassword = VALUES(localpassword);

TRUNCATE TABLE gmd.user_map;

INSERT INTO gmd.user_map(old_userid, uid_final, email)
SELECT old_userid, uid_final, email
FROM gmd.users_stage;

/* 7) PERMESSI -> gmd.permissions (modalità 2) */
INSERT IGNORE INTO gmd.permissions(operator, dc)
SELECT go.uid, mo.dc
FROM gmd.operators go
JOIN magazzino.operators mo
  ON mo.op = go.uid
WHERE go.active = 1
  AND mo.dc IS NOT NULL;

SET sql_safe_updates = 1;

UPDATE gmd.operators SET localpassword = 'PASSWORD' WHERE uid = 'MANZOGI9';