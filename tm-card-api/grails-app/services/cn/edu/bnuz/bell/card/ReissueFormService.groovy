package cn.edu.bnuz.bell.card

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.organization.Student
import cn.edu.bnuz.bell.system.SystemConfigService
import cn.edu.bnuz.bell.workflow.DomainStateMachineHandler
import cn.edu.bnuz.bell.workflow.State
import cn.edu.bnuz.bell.workflow.commands.SubmitCommand
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
    SystemConfigService systemConfigService

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
    def list(String studentId) {
        if (!pictureExists(studentId)) {
            return [
                    warning: systemConfigService.get(CardReissueForm.CONFIG_NO_PICTURE, 'No picture.')
            ]
        }

        def forms = CardReissueForm.executeQuery '''
select new Map(
  form.id as id,
  form.ordinal as ordinal,
  form.status as status
)
from CardReissueForm form
join form.student student
where student.id = :studentId
''', [studentId: studentId]
        return [forms: forms]
    }

    def getStudentInfo(String studentId) {
        def results = Student.executeQuery '''
select new map(
  student.id as id,
  student.name as name,
  student.sex as sex,
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

    Map getFormInfo(Long id) {
        def results = CardReissueForm.executeQuery '''
select new map(
  form.id as id,
  form.reason as reason,
  form.status as status,
  form.student.id as studentId,
  form.ordinal as ordinal,
  form.workflowInstance.id as workflowInstanceId
)
from CardReissueForm form
where form.id = :id
''', [id: id]
        if (!results) {
            throw new NotFoundException()
        }

        return results ? results[0] : null
    }

    def getFormForShow(String studentId, Long id) {
        def form = getFormInfo(id)

        if (!form) {
            throw new NotFoundException()
        }

        if (form.studentId != studentId) {
            throw new ForbiddenException()
        }

        return [
                form    : form,
                student : getStudentInfo(form.studentId as String),
                editable: domainStateMachineHandler.canUpdate(form),
        ]
    }

    def getFormForCreate(String studentId) {
        def student = Student.load(studentId)
        def count = checkFormCount(student)

        return [
                form   : [
                        studentId: studentId,
                        ordinal  : count + 1,
                ],
                student: getStudentInfo(studentId),
        ]
    }

    def getFormForEdit(String studentId, Long id) {
        def form = getFormInfo(id)

        if (!form) {
            throw new NotFoundException()
        }

        if (form.studentId != studentId) {
            throw new ForbiddenException()
        }

        if (!domainStateMachineHandler.canUpdate(form)) {
            throw new BadRequestException()
        }

        return [form: form, student: getStudentInfo(form.studentId as String)]
    }

    CardReissueForm create(String studentId, CardReissueFormCommand cmd) {
        def student = Student.load(studentId)
        def count = checkFormCount(student)

        def now = new Date()
        CardReissueForm form = new CardReissueForm(
                student: student,
                reason: cmd.reason,
                dateCreated: now,
                dateModified: now,
                ordinal: count + 1,
                status: domainStateMachineHandler.initialState,
        )

        form.save()

        domainStateMachineHandler.create(form, studentId)

        return form
    }


    CardReissueForm update(String studentId, CardReissueFormCommand cmd) {
        CardReissueForm form = CardReissueForm.get(cmd.id)

        if (!form) {
            throw new NotFoundException()
        }

        if (form.student.id != studentId) {
            throw new ForbiddenException()
        }

        if (!domainStateMachineHandler.canUpdate(form)) {
            throw new BadRequestException()
        }

        form.reason = cmd.reason
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

        if (form.workflowInstance) {
            form.workflowInstance.delete()
        }

        form.delete()
    }

    void submit(String studentId, SubmitCommand cmd) {
        CardReissueForm form = CardReissueForm.get(cmd.id)

        if (!form) {
            throw new NotFoundException()
        }

        if (form.student.id != studentId) {

            throw new ForbiddenException()
        }

        if (!domainStateMachineHandler.canSubmit(form)) {
            throw new BadRequestException()
        }

        domainStateMachineHandler.submit(form, studentId, cmd.to, cmd.comment, cmd.title)

        form.dateSubmitted = new Date()
        form.save()
    }

    Integer checkFormCount(Student student) {
        def totalCount = CardReissueForm.countByStudent(student)
        def maxCount = systemConfigService.get(CardReissueForm.CONFIG_MAX_COUNT, 2)

        if (totalCount >= maxCount) {
            throw new BadRequestException("申请次数已经超过${maxCount}次。")
        } else if (totalCount > 0) {
            def unfinished = CardReissueForm.countByStudentAndStatusNotEqual(student, State.FINISHED)
            if (unfinished > 0) {
                throw new BadRequestException('存在未完成的申请。')
            }
        }
        return totalCount
    }

    def getNotice() {
        [
                title: '补办学生证须知',
                content: systemConfigService.get(CardReissueForm.CONFIG_NOTICE, ''),
                maxCount: systemConfigService.get(CardReissueForm.CONFIG_MAX_COUNT, 2)
        ]
    }
}
