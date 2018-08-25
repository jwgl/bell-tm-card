package cn.edu.bnuz.bell.card

import grails.gorm.transactions.Transactional

@Transactional
class ReissueService {
    ReissueFormService reissueFormService

    def getForm(Long id) {
        def form = reissueFormService.getFormInfo(id)
        return [
                form    : form,
                student : reissueFormService.getStudentInfo(form.studentId as String),
        ]
    }
}
