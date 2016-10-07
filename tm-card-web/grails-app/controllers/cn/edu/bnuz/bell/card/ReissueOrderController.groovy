package cn.edu.bnuz.bell.card

import cn.edu.bnuz.bell.report.ReportClientService
import cn.edu.bnuz.bell.report.ReportRequest
import cn.edu.bnuz.bell.report.ReportResponse
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize

/**
 * 补办学生证订单。
 * @author Yang Lin
 */
@PreAuthorize('hasAuthority("PERM_CARD_REISSUE_CHECK")')
class ReissueOrderController {
    ReissueOrderService reissueOrderService
    ReportClientService reportClientService

    def index() { }

    def productionOrder(Long reissueOrderId) {
        ReportResponse reportResponse = reportClientService.runAndRender(new ReportRequest(
                reportService: 'tm-report',
                reportName: 'card-reissue-order',
                format: 'xlsx',
                parameters: [orderId: reissueOrderId]
        ))

        if (reportResponse.statusCode == HttpStatus.OK) {
            response.setHeader('Content-Disposition', reportResponse.contentDisposition)
            response.outputStream << reportResponse.content
        } else {
            response.setStatus(reportResponse.statusCode.value())
        }
    }

    def distributionList(Long reissueOrderId) {
        ReportResponse reportResponse = reportClientService.runAndRender(new ReportRequest(
                reportService: 'tm-report',
                reportName: 'card-reissue-distribute',
                format: 'xlsx',
                parameters: [orderId: reissueOrderId]
        ))

        if (reportResponse.statusCode == HttpStatus.OK) {
            response.setHeader('Content-Disposition', reportResponse.contentDisposition)
            response.outputStream << reportResponse.content
        } else {
            response.setStatus(reportResponse.statusCode.value())
        }
    }

    def pictures(Long reissueOrderId) {
        byte[] data  = reissueOrderService.pictures(reissueOrderId)
        response.setHeader("Content-disposition", "attachment; filename=${URLEncoder.encode('学生照片', 'UTF-8')}-${reissueOrderId}.zip")
        response.outputStream << data
    }
}
