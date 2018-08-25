package cn.edu.bnuz.bell.card

import cn.edu.bnuz.bell.report.ReportClientService
import cn.edu.bnuz.bell.report.ReportRequest
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_CARD_REISSUE_APPROVE")')
class ReissueOrderReportController {
    ReissueOrderReportService reissueOrderReportService
    ReportClientService reportClientService

    def productionOrder(Long reissueOrderId) {
        def reportRequest = new ReportRequest(
                reportName: 'card-reissue-order',
                parameters: [orderId: reissueOrderId]
        )
        reportClientService.runAndRender(reportRequest, response)
    }

    def distributionList(Long reissueOrderId) {
        def reportRequest = new ReportRequest(
                reportName: 'card-reissue-distribute',
                parameters: [orderId: reissueOrderId]
        )
        reportClientService.runAndRender(reportRequest, response)
    }

    def pictures(Long reissueOrderId) {
        render(
                file: reissueOrderReportService.pictures(reissueOrderId),
                contentType: 'application/zip',
                fileName: "${URLEncoder.encode('学生照片', 'UTF-8')}-${reissueOrderId}.zip"
        )
    }
}
