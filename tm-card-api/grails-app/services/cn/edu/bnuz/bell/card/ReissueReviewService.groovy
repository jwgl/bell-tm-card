package cn.edu.bnuz.bell.card

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.security.User
import cn.edu.bnuz.bell.workflow.*
import cn.edu.bnuz.bell.workflow.commands.AcceptCommand
import cn.edu.bnuz.bell.workflow.commands.RejectCommand
import grails.transaction.Transactional

@Transactional
class ReissueReviewService extends AbstractReviewService{
    ReissueFormService reissueFormService
    DomainStateMachineHandler domainStateMachineHandler

    /**
     * 各状态申请数量
     * @return 各状态申请数量
     */
    def getCountsByStatus() {
        def results = CardReissueForm.executeQuery("""
select status, count(*)
from CardReissueForm
group by status
""")
        return results.collectEntries {[it[0].name(), it[1]]}
    }

    /**
     * 查找所有指定状态的申请（DTO）
     * @param status
     * @param offset
     * @param max
     * @return
     */
    def findAllByStatus(State status, int offset, int max) {
        CardReissueForm.executeQuery """
select new map(
  form.id as id,
  student.id as studentId,
  student.name as name,
  student.sex as sex,
  department.name as department,
  subject.name as subject,
  form.dateModified as applyDate,
  form.status as status,
  formRank.rank as rank
)
from CardReissueFormRank formRank
join formRank.form as form
join form.student student
join student.major major
join major.subject subject
join student.department department
where form.status = :status
order by form.dateModified desc
""", [status: status], [offset: offset, max: max]
    }

    def getFormForReview(String userId, Long id) {
        def activity = Activities.CHECK
        def form = reissueFormService.getFormInfo(id)
        def workitem = Workitem.findByInstanceAndActivityAndToAndDateProcessedIsNull(
                WorkflowInstance.load(form.workflowInstanceId),
                WorkflowActivity.load("${CardReissueForm.WORKFLOW_ID}.${activity}"),
                User.load(userId),
        )
        if (workitem) {
            form.workitemId = workitem.id
        }

        checkReviewer(id, activity, userId)

        return form
    }

    def getFormForReview(String userId, Long id, UUID workitemId) {
        def form = reissueFormService.getFormInfo(id)

        def workitem = Workitem.get(workitemId)
        if (!workitem ||
            workitem.instance.id != form.workflowInstanceId ||
            workitem.to.id != userId) {
            throw new BadRequestException()
        }

        checkReviewer(id, workitem.activitySuffix, userId)

        return form
    }

    /**
     * 同意
     * @param cmd 同意数据
     * @param userId 用户ID
     * @param workItemId 工作项ID
     */
    void accept(AcceptCommand cmd, String userId, UUID workitemId) {
        CardReissueForm form = CardReissueForm.get(cmd.id)

        if (!form) {
            throw new NotFoundException()
        }

        if (!domainStateMachineHandler.canAccept(form)) {
            throw new BadRequestException()
        }

        def activity = Workitem.get(workitemId).activitySuffix
        checkReviewer(cmd.id, activity, userId)

        domainStateMachineHandler.accept(form, userId, cmd.comment, workitemId)

        form.save()
    }

    /**
     * 不同意
     * @param cmd 不同意数据
     * @param userId 用户ID
     * @param workItemId 工作项ID
     */
    void reject(RejectCommand cmd, String userId, UUID workitemId) {
        CardReissueForm form = CardReissueForm.get(cmd.id)

        if (!form) {
            throw new NotFoundException()
        }

        if (!domainStateMachineHandler.canReject(form)) {
            throw new BadRequestException()
        }


        def activity = Workitem.get(workitemId).activitySuffix
        checkReviewer(cmd.id, activity, userId)

        domainStateMachineHandler.reject(form, userId, cmd.comment, workitemId)

        form.save()
    }

    List<Map> getReviewers(String type, Long id) {
        switch (type) {
            case Activities.CHECK:
                return reissueFormService.getCheckers()
            default:
                throw new BadRequestException()
        }
    }
}
