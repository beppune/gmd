package it.posteitaliane.gdc.gadc.services

import com.lowagie.text.pdf.codec.Base64.InputStream
import jakarta.annotation.PreDestroy
import org.eclipse.birt.core.framework.Platform
import org.eclipse.birt.report.engine.api.EXCELRenderOption
import org.eclipse.birt.report.engine.api.EngineConfig
import org.eclipse.birt.report.engine.api.EngineConstants
import org.eclipse.birt.report.engine.api.IReportEngine
import org.eclipse.birt.report.engine.api.IReportEngineFactory
import org.eclipse.birt.report.engine.api.PDFRenderOption
import org.eclipse.core.internal.registry.RegistryProviderFactory
import org.springframework.stereotype.Service
import org.springframework.util.ResourceUtils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import javax.sql.DataSource

@Service
class ReportService(private val ds:DataSource
) {

    val engine:IReportEngine

    init {

        val config = EngineConfig()

        Platform.startup(config)

        engine = (Platform
            .createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY) as IReportEngineFactory)
            .createReportEngine(config)

    }

    fun runreport() : ByteArrayInputStream {
        println("TEST REPORT")
        val report = engine.openReportDesign( ResourceUtils.getFile ("classpath:transactions.rptdesign").path )

        val task = engine.createRunAndRenderTask(report)

        task.appContext.put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, ReportService::class.java.classLoader)
        task.appContext.put("OdaJDBCDriverPassInConnection", ds.connection)
        task.appContext.put("OdaJDBCDriverPassInConnectionCloseAfterUse", false);

        val pdfOptions = PDFRenderOption().apply {
            outputFormat = "pdf"
            outputFileName = "test.pdf"
        }

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