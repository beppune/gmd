package it.posteitaliane.gdc.gadc.services

import it.posteitaliane.gdc.gadc.views.transactions.TransactionFilter
import jakarta.annotation.PreDestroy
import org.eclipse.birt.core.framework.Platform
import org.eclipse.birt.report.engine.api.*
import org.eclipse.core.internal.registry.RegistryProviderFactory
import org.springframework.stereotype.Service
import org.springframework.util.ResourceUtils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.sql.DataSource

@Service
class ReportService(
    private val ds:DataSource
) {

    final val engine:IReportEngine

    final val formatter:DateTimeFormatter

    init {

        val config = EngineConfig()

        Platform.startup(config)

        engine = (Platform
            .createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY) as IReportEngineFactory)
            .createReportEngine(config)

        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")
    }

    fun runreport(filter: TransactionFilter): ByteArrayInputStream {
        println("SERVICE: $filter")
        val report = engine.openReportDesign( ResourceUtils.getFile ("classpath:transactions2.rptdesign").path )

        val task = engine.createRunAndRenderTask(report)

        task.appContext.put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, ReportService::class.java.classLoader)
        task.appContext.put("OdaJDBCDriverPassInConnection", ds.connection)
        task.appContext.put("OdaJDBCDriverPassInConnectionCloseAfterUse", false);

        val from = filter.from ?: LocalDateTime.of(2000, 1, 1, 0, 0, 0)
        val to = filter.to ?: LocalDateTime.of(2050, 12, 31, 23, 59, 59)

        task.setParameterValue("dcParam", "${filter.dc?.short ?: '%'}%")
        task.setParameterValue("fromParam", from.format(formatter))
        task.setParameterValue("toParam", to.format(formatter))

        val out = ByteArrayOutputStream()

        val xlsxOptions = EXCELRenderOption().apply {
            outputFormat = "xlsx"
            outputFileName = "test.xlsx"
            outputStream = out
        }

        task.renderOption = xlsxOptions

        task.run()
        task.close()

        val result = ByteArrayInputStream(out.toByteArray())
        return result
    }

    @PreDestroy
    fun destroy() {
        engine.destroy()
        Platform.shutdown()
        RegistryProviderFactory.releaseDefault()
    }

}