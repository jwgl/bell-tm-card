package cn.edu.bnuz.bell.card

import cn.edu.bnuz.bell.report.ZipService
import grails.gorm.transactions.Transactional
import org.springframework.beans.factory.annotation.Value

@Transactional
class ReissueOrderReportService {
    @Value('${bell.student.picturePath}')
    String picturePath

    ZipService zipService

    byte[] pictures(Long orderId) {
        def studentIds = CardReissueOrder.executeQuery '''
select reissueForm.student.id
from CardReissueOrder reissueOrder
join reissueOrder.items reissueOrderItem
join reissueOrderItem.form reissueForm
where reissueOrder.id =:orderId''', [orderId: orderId]

        File[] files = studentIds.collect {
            new File(picturePath, "${it}.jpg")
        }
        zipService.zip(files)
    }
}
