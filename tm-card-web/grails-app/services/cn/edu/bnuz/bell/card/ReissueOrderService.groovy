package cn.edu.bnuz.bell.card

import cn.edu.bnuz.bell.report.ZipService
import grails.plugins.rest.client.RestBuilder
import org.grails.web.util.WebUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.client.RestTemplate

class ReissueOrderService {
    @Value('${bell.student.picturePath}')
    String picturePath

    ZipService zipService
    RestTemplate restTemplate

    byte[] pictures(Long orderId) {
        RestBuilder rest = new RestBuilder(restTemplate)
        def restResponse = rest.get('http://tm-card-api/reissueOrders/{orderId}', [orderId: orderId]) {
            auth WebUtils.retrieveGrailsWebRequest().currentRequest.getHeader('authorization')
        }

        File[] files = restResponse.json.items.collect {
            new File(picturePath, "${it.studentId}.jpg")
        }
        zipService.zip(files)
    }
}
