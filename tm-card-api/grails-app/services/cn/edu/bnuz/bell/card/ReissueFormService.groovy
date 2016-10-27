package cn.edu.bnuz.bell.card

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.organization.Student
import cn.edu.bnuz.bell.security.User
import cn.edu.bnuz.bell.workflow.CommitCommand
import cn.edu.bnuz.bell.workflow.DomainStateMachineHandler
import cn.edu.bnuz.bell.workflow.States
import grails.transaction.Transactional
import org.springframework.beans.factory.annotation.Value

import java.nio.file.Files
import java.nio.file.Paths

/**
 * 补办学生证申请服务
 * @author Yang Lin
 */
@Transactional
class ReissueFormService {
    DomainStateMachineHandler domainStateMachineHandler

    @Value('${bell.student.picturePath}')
    String picturePath

    /**
     * 照片文件是否存在
     * @param studentId 学生ID
     * @return 照片文件是否存在
     */
    boolean pictureExists(String studentId) {
        Files.exists(Paths.get(picturePath, "${studentId}.jpg"))
    }

    /**
     * 查找学生所有申请
     * @param studentId
     * @return 申请列表
     */
    def getForms(String studentId) {
        CardReissueForm.executeQuery '''
select new Map(
  form.id as id,
  form.status as status
)
from CardReissueForm form
join form.student student
where student.id = :studentId
''', [studentId: studentId]
    }

    private getStudent(String studentId) {
        def results = Student.executeQuery '''
select new map(
  student.id as id,
  student.name as name,
  student.birthday as birthday,
  admission.fromProvince as province,
  department.name as department,
  subject.name as subject,
  '本科' as educationLevel)
from Student student
join student.admission admission
join student.department department
join student.major m
join m.subject subject
where student.id = :studentId
''', [studentId: studentId]
        if (!results) {
            throw new NotFoundException()
        }
        return results[0]
    }

    def getFormInfo(Long id) {
        def results = CardReissueForm.executeQuery '''
select new map(
  form.id as id,
  form.reason as reason,
  form.status as status,
  form.student.id as studentId,
  form.workflowInstance.id as workflowInstanceId
)
from CardReissueForm form
where form.id = :id
''', [id: id]
        if (!results) {
            throw new NotFoundException()
        }

        def form = results[0]
        form.student = getStudent(form.studentId)
        form.remove('studentId')

        return form
    }

    def getFormForShow(String studentId, Long id) {
        def form = getFormInfo(id)

        if (form.student.id != studentId) {
            throw new ForbiddenException()
        }

        form.editable = domainStateMachineHandler.canUpdate(form)

        return form
    }

    def getFormForCreate(String studentId) {
        return [student: getStudent(studentId)]
    }

    def getFormForEdit(String studentId, Long id) {
        def form = getFormInfo(id)

        if (form.student.id != studentId) {
            throw new ForbiddenException()
        }

        if (!domainStateMachineHandler.canUpdate(form)) {
            throw new BadRequestException()
        }

        return form
    }

    CardReissueForm create(String studentId, String reason) {
        def student = Student.load(studentId)
        def totalCount = CardReissueForm.countByStudent(student)

        if (totalCount >= 2) {
            throw new BadRequestException('申请次数已经超过2次。')
        } else if (totalCount > 0) {
            def unfinished = CardReissueForm.countByStudentAndStatusNotEqual(student, States.FINISHED)
            if (unfinished > 0) {
                throw new BadRequestException('存在未完成的申请。')
            }
        }

        def now = new Date()
        CardReissueForm form = new CardReissueForm(
                student: student,
                reason: reason,
                dateCreated: now,
                dateModified: now,
                status: domainStateMachineHandler.initialState,
        )

        form.save()

        domainStateMachineHandler.create(form, studentId)

        return form
    }


    CardReissueForm update(String studentId, Long id, String reason) {
        CardReissueForm form = CardReissueForm.get(id)

        if (!form) {
            throw new NotFoundException()
        }

        if (form.student.id != studentId) {
            throw new ForbiddenException()
        }

        if (!domainStateMachineHandler.canUpdate(form)) {
            throw new BadRequestException()
        }

        form.reason = reason
        form.dateModified = new Date()

        domainStateMachineHandler.update(form, studentId)

        form.save()
    }

    void delete(String studentId, Long id) {
        CardReissueForm form = CardReissueForm.get(id)

        if (!form) {
            throw new NotFoundException()
        }

        if (form.student.id != studentId) {
            throw new ForbiddenException()
        }

        if (!domainStateMachineHandler.canUpdate(form)) {
            throw new BadRequestException()
        }

        //userLogService.log(AuditAction.DELETE, form)

        if (form.workflowInstance) {
            form.workflowInstance.delete()
        }

        form.delete()
    }

    void commit(String studentId, CommitCommand cmd) {
        CardReissueForm form = CardReissueForm.get(cmd.id)

        if (!form) {
            throw new NotFoundException()
        }

        if (form.student.id != studentId) {

            throw new ForbiddenException()
        }

        if (!domainStateMachineHandler.canCommit(form)) {
            throw new BadRequestException()
        }

        domainStateMachineHandler.commit(form, studentId, cmd.to, cmd.comment, cmd.title)

        form.save()
    }

    def getCheckers() {
        User.findAllWithPermission('PERM_CARD_REISSUE_CHECK')
    }
}
