package cn.edu.bnuz.bell.card

import cn.edu.bnuz.bell.workflow.State
import grails.transaction.Transactional

@Transactional
class ReissueService {
    def findAllByStatus(State status) {
        CardReissueForm.executeQuery '''
select new map(
  form.id as id,
  student.id as studentId,
  student.name as name,
  student.sex as sex,
  department.name as department,
  subject.name as subject,
  form.dateModified as applyDate,
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
}
