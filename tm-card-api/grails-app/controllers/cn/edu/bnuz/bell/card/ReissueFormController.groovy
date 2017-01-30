package cn.edu.bnuz.bell.card

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.http.ServiceExceptionHandler
import cn.edu.bnuz.bell.organization.Student
import cn.edu.bnuz.bell.workflow.commands.SubmitCommand
import cn.edu.bnuz.bell.workflow.Event
import org.springframework.security.access.prepost.PreAuthorize

/**
 * 学生申请补办学生证
 * @author Yang Lin
 */
@PreAuthorize('hasAuthority("PERM_CARD_REISSUE_WRITE")')
class ReissueFormController implements ServiceExceptionHandler {
    ReissueFormService reissueFormService

    def index(String studentId) {
        Student student = Student.findById(studentId)
        if (student == null) {
            throw new NotFoundException()
        }

        renderJson([
                student: [
                        id      : student.id,
                        atSchool: student.atSchool,
                        picture : reissueFormService.pictureExists(student.id)
                ],
                forms  : reissueFormService.getForms(student.id)
        ])
    }

    def show(String studentId, Long id) {
        renderJson reissueFormService.getFormForShow(studentId, id)
    }

    def create(String studentId) {
        renderJson reissueFormService.getFormForCreate(studentId)
    }

    def save(String studentId) {
        def form = reissueFormService.create(studentId, request.JSON.reason as String)
        renderJson([id: form.id])
    }

    def edit(String studentId, Long id) {
        renderJson reissueFormService.getFormForEdit(studentId, id)
    }

    def update(String studentId, Long id) {
        reissueFormService.update(studentId, id, request.JSON.reason as String)
        renderOk()
    }

    def delete(String studentId, Long id) {
        reissueFormService.delete(studentId, id)
        renderOk()
    }

    def patch(String studentId, Long id, String op) {
        def operation = Event.valueOf(op)
        switch (operation) {
            case Event.SUBMIT:
                def cmd = new SubmitCommand()
                bindData(cmd, request.JSON)
                cmd.id = id
                reissueFormService.submit(studentId, cmd)
                renderOk()
                break
            default:
                throw new BadRequestException()
        }
    }

    def checkers(String studentId, Long reissueFormId) {
        renderJson reissueFormService.getCheckers()
    }
}
