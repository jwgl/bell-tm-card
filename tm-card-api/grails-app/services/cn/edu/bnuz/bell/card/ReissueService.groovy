package cn.edu.bnuz.bell.card

import cn.edu.bnuz.bell.workflow.State
import grails.gorm.transactions.Transactional

@Transactional
class ReissueService {
    ReissueFormService reissueFormService

    def findAllByStatus(State status) {
        CardReissueForm.executeQuery '''
select new map(
  form.id as id,
  student.id as studentId,
  student.name as name,
  student.sex as sex,
  department.name as department,
  subject.name as subject,
  form.dateSubmitted as dateSubmitted,
  form.dateApproved as dateApproved,
  form.status as status
)
from CardReissueForm form
join form.student student
join student.major major
join major.subject subject
join student.department department
where form.status = :status
order by form.dateModified desc
''', [status: status]
    }

    def getForm(Long id) {
        def form = reissueFormService.getFormInfo(id)
        return [
                form    : form,
                student : reissueFormService.getStudentInfo(form.studentId as String),
        ]
    }
}
