package cn.edu.bnuz.bell.card

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.workflow.Activities
import cn.edu.bnuz.bell.workflow.Workitem
import grails.transaction.Transactional

/**
 * 补办学生证审核服务
 * @author Yang Lin
 */
@Transactional
class ReissueReviewService {
    ReissueFormService cardReissueFormService
    /**
     * 获取审核数据
     * @param id Vision ID
     * @param userId 用户ID
     * @param workitemId 工作项ID
     * @return 审核数据
     */
    def getFormForReview(Long id, String userId, UUID workitemId) {
        def form = cardReissueFormService.getFormInfo(id)

        Workitem workitem = Workitem.get(workitemId)
        def activity = workitem.activitySuffix
        switch (activity) {
            case Activities.CHECK:
                if (!isChecker(id, userId)) {
                    throw new ForbiddenException()
                }
                break;
            default:
                throw new BadRequestException()
        }

        return form
    }
}
