package cn.edu.bnuz.bell.card

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.security.User
import cn.edu.bnuz.bell.workflow.Activities
import cn.edu.bnuz.bell.workflow.ReviewerProvider
import grails.gorm.transactions.Transactional

@Transactional(readOnly = true)
class ReissueReviewerService implements ReviewerProvider{
    List<Map> getReviewers(Object id, String activity) {
        switch (activity) {
            case Activities.APPROVE:
                return getApprovers()
            default:
                throw new BadRequestException()
        }
    }

    def getApprovers() {
        User.findAllWithPermission('PERM_CARD_REISSUE_APPROVE')
    }
}
