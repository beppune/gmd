package it.posteitaliane.gdc.gadc.services.reports

import jakarta.servlet.annotation.WebServlet
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.web.servlet.ServletComponentScan

@ServletComponentScan
@WebServlet(name = "ReportsServlet", urlPatterns = ["/reports"])
class ReportsServlet : HttpServlet() {

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        super.doGet(req, resp)

        resp.writer.print("Hello")

    }
}