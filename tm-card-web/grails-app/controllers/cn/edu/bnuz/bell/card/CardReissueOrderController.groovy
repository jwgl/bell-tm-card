package cn.edu.bnuz.bell.card

import cn.edu.bnuz.bell.report.ReportClientService
import cn.edu.bnuz.bell.report.ReportRequest
import cn.edu.bnuz.bell.report.ReportResponse
import org.springframework.http.HttpStatus
import org.springframework.security.access.annotation.Secured

/**
 * 补办学生证订单。
 * @author Yang Lin
 */
@Secured(['ROLE_PERM_CARD_REISSUE_CHECK'])
class CardReissueOrderController {
    CardReissueOrderService cardReissueOrderService
    ReportClientService reportClientService

    def index() { }

    def productionOrder(Long cardReissueOrderId) {
        ReportResponse reportResponse = reportClientService.runAndRender(new ReportRequest(
                reportService: 'tm-report',
                reportName: 'card-reissue-order',
                format: 'xlsx',
                parameters: [orderId: cardReissueOrderId]
        ))

        if (reportResponse.statusCode == HttpStatus.OK) {
            response.setHeader('Content-Disposition', reportResponse.contentDisposition)
            response.outputStream << reportResponse.content
        } else {
            response.setStatus(reportResponse.statusCode.value())
        }
    }

    def distributionList(Long cardReissueOrderId) {
        ReportResponse reportResponse = reportClientService.runAndRender(new ReportRequest(
                reportService: 'tm-report',
                reportName: 'card-reissue-distribute',
                format: 'xlsx',
                parameters: [orderId: cardReissueOrderId]
        ))

        if (reportResponse.statusCode == HttpStatus.OK) {
            response.setHeader('Content-Disposition', reportResponse.contentDisposition)
            response.outputStream << reportResponse.content
        } else {
            response.setStatus(reportResponse.statusCode.value())
        }
    }

    def pictures(Long cardReissueOrderId) {
        byte[] data  = cardReissueOrderService.pictures(cardReissueOrderId)
        response.setHeader("Content-disposition", "attachment; filename=${URLEncoder.encode('学生照片', 'UTF-8')}-${cardReissueOrderId}.zip")
        response.outputStream << data
    }
}
