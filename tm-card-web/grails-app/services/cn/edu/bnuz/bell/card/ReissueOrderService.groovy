package cn.edu.bnuz.bell.card

import cn.edu.bnuz.bell.report.ZipService
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.client.OAuth2RestOperations

import javax.annotation.Resource

class ReissueOrderService {
    @Value('${bell.student.picturePath}')
    String picturePath

    ZipService zipService

    @Resource(name="reportRestTemplate")
    OAuth2RestOperations restTemplate

    byte[] pictures(Long orderId) {
        def restResponse = restTemplate.getForObject('http://tm-card-api/reissueOrders/{orderId}', String, [orderId: orderId])
        def order = new JsonSlurper().parseText(restResponse)
        File[] files = order.items.collect {
            new File(picturePath, "${it.studentId}.jpg")
        }
        zipService.zip(files)
    }
}
