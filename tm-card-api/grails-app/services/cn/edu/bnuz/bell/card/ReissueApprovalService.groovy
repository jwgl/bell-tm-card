package cn.edu.bnuz.bell.card

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.organization.Teacher
import cn.edu.bnuz.bell.security.User
import cn.edu.bnuz.bell.service.DataAccessService
import cn.edu.bnuz.bell.workflow.*
import cn.edu.bnuz.bell.workflow.commands.AcceptCommand
import cn.edu.bnuz.bell.workflow.commands.RejectCommand
import grails.transaction.Transactional

@Transactional
class ReissueApprovalService {
    ReissueFormService reissueFormService
    DomainStateMachineHandler domainStateMachineHandler
    DataAccessService dataAccessService

    def getCounts(String userId) {
        [
                (ListType.TODO): CardReissueForm.countByStatus(State.SUBMITTED),
                (ListType.DONE): CardReissueForm.countByApprover(Teacher.load(userId))
        ]
    }

    def list(String userId, ListCommand cmd) {
        switch (cmd.type) {
            case ListType.TODO:
                return findTodoList(userId, cmd)
            case ListType.DONE:
                return findDoneList(userId, cmd)
            default:
                throw new BadRequestException()
        }
    }

    def findTodoList(String userId, ListCommand cmd) {
        def forms = CardReissueForm.executeQuery '''
select new map(
  form.id as id,
  student.id as studentId,
  student.name as studentName,
  student.sex as sex,
  department.name as department,
  subject.name as subject,
  form.dateSubmitted as date,
  form.status as status,
  form.ordinal as ordinal
)
from CardReissueForm form
join form.student student
join student.major major
join major.subject subject
join student.department department
where form.status = :status
order by form.dateSubmitted
''', [status: State.SUBMITTED], [offset: cmd.offset, max: cmd.max]

        return [forms: forms, counts: getCounts(userId)]
    }

    def findDoneList(String userId, ListCommand cmd) {
        def forms = CardReissueForm.executeQuery '''
select new map(
  form.id as id,
  student.id as studentId,
  student.name as studentName,
  student.sex as sex,
  department.name as department,
  subject.name as subject,
  form.dateApproved as date,
  form.status as status,
  form.ordinal as ordinal
)
from CardReissueForm form
join form.student student
join student.major major
join major.subject subject
join student.department department
where form.approver.id = :userId
order by form.dateApproved desc
''', [userId: userId], [offset: cmd.offset, max: cmd.max]

        return [forms: forms, counts: getCounts(userId)]
    }

    def getFormForReview(String userId, Long id, ListType type) {
        def form = reissueFormService.getFormInfo(id)

        def workitem = Workitem.findByInstanceAndActivityAndToAndDateProcessedIsNull(
                WorkflowInstance.load(form.workflowInstanceId),
                WorkflowActivity.load("${CardReissueForm.WORKFLOW_ID}.${Activities.APPROVE}"),
                User.load(userId),
        )
        if (workitem) {
            form.workitemId = workitem.id
        }

        domainStateMachineHandler.checkReviewer(id, userId, Activities.APPROVE)

        return [
                form: form,
                student: reissueFormService.getStudentInfo(form.studentId),
                counts: getCounts(userId),
                workitemId: workitem ? workitem.id : null,
                prevId: getPrevReviewId(userId, id, type),
                nextId: getNextReviewId(userId, id, type),
        ]
    }

    def getFormForReview(String userId, Long id, ListType type, UUID workitemId) {
        def form = reissueFormService.getFormInfo(id)

        def activity = Workitem.get(workitemId).activitySuffix
        domainStateMachineHandler.checkReviewer(id, userId, activity)

        return [
                form: form,
                student: reissueFormService.getStudentInfo(form.studentId),
                counts: getCounts(userId),
                workitemId: workitemId,
                prevId: getPrevReviewId(userId, id, type),
                nextId: getNextReviewId(userId, id, type),
        ]
    }

    Long getPrevReviewId(String userId, Long id, ListType type) {
        switch (type) {
            case ListType.TODO:
                return dataAccessService.getLong('''
select form.id
from CardReissueForm form
where form.status = :status
and form.dateSubmitted < (select dateSubmitted from CardReissueForm where id = :id)
order by form.dateSubmitted desc
''', [id: id, status: State.SUBMITTED])
            case ListType.DONE:
                return dataAccessService.getLong('''
select form.id
from CardReissueForm form
where form.approver.id = :userId
and form.dateApproved > (select dateApproved from CardReissueForm where id = :id)
order by form.dateApproved asc
''', [id: id, userId: userId])
        }
    }

    Long getNextReviewId(String userId, Long id, ListType type) {
        switch (type) {
            case ListType.TODO:
                return dataAccessService.getLong('''
select form.id
from CardReissueForm form
where form.status = :status
and form.dateSubmitted > (select dateSubmitted from CardReissueForm where id = :id)
order by form.dateSubmitted asc
''', [id: id, status: State.SUBMITTED])
            case ListType.DONE:
                return dataAccessService.getLong('''
select form.id
from CardReissueForm form
where form.approver.id = :userId
and form.dateApproved < (select dateApproved from CardReissueForm where id = :id)
order by form.dateApproved desc
''', [id: id, userId: userId])
        }
    }

    /**
     * 同意
     * @param cmd 同意数据
     * @param userId 用户ID
     * @param workItemId 工作项ID
     */
    void accept(AcceptCommand cmd, String userId, UUID workitemId) {
        CardReissueForm form = CardReissueForm.get(cmd.id)
        domainStateMachineHandler.accept(form, userId, Activities.APPROVE, cmd.comment, workitemId)
        form.approver = Teacher.load(userId)
        form.dateApproved = new Date()
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
        domainStateMachineHandler.reject(form, userId, Activities.APPROVE, cmd.comment, workitemId)
        form.approver = Teacher.load(userId)
        form.dateApproved = new Date()
    }
}
