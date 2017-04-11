package cn.edu.bnuz.bell.card

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.organization.Teacher
import cn.edu.bnuz.bell.workflow.DomainStateMachineHandler
import cn.edu.bnuz.bell.workflow.State
import grails.transaction.Transactional

@Transactional
class ReissueOrderService {
    DomainStateMachineHandler domainStateMachineHandler

    def list(Integer offset, Integer max) {
        def orders = CardReissueOrder.executeQuery '''
select new map(
  o.id as id,
  count(oi.id) as totalCount,
  sum(case when form.status = :status then 1 else 0 end) as finishedCount,
  creator.name as creatorName,
  o.dateCreated as dateCreated,
  modifier.name as modifierName,
  o.dateModified as dateModified
)
from CardReissueOrder o
join o.items oi
join oi.form form
join o.creator creator
left join o.modifier modifier
group by o.id, creator.name, o.dateCreated, modifier.name, o.dateModified
order by o.dateCreated desc
''', [status: State.FINISHED], [offset: offset, max: max]
        def count = CardReissueOrder.count()
        return [
                orders: orders,
                count: count,
        ]
    }

    def getInfo(Long id) {
        def results = CardReissueOrder.executeQuery '''
select new map(
  o.id as id,
  creator.name as creatorName,
  o.dateCreated as dateCreated,
  modifier.name as modifierName,
  o.dateModified as dateModified
)
from CardReissueOrder o
join o.creator creator
left join o.modifier modifier
where o.id = :id
''', [id: id]

        if (!results) {
            throw new NotFoundException()
        }

        def order = results[0]
        order.items = CardReissueOrderItem.executeQuery '''
select new map(
  oi.id as id,
  form.id as formId,
  form.dateSubmitted as dateSubmitted,
  form.dateApproved as dateApproved,
  form.status as status,
  student.id as studentId,
  student.name as name,
  student.sex as sex,
  admission.fromProvince as province,
  student.birthday as birthday,
  concat(major.grade + subject.lengthOfSchooling, '-7-1') as validDate,
  adminClass.name as adminClass,
  department.name as department,
  subject.name as subject
)
from CardReissueOrderItem oi
join oi.form form
join form.student student
join student.admission admission
join student.adminClass adminClass
join student.department department
join student.major major
join major.subject subject
where oi.order.id = :id
order by student.id
''', [id: id]

        return order
    }

    CardReissueOrder create(String userId, CardReissueOrderCommand cmd) {
        CardReissueOrder order = new CardReissueOrder(
                creator: Teacher.load(userId),
                dateModified: new Date(),
        )

        cmd.addedItems.each {
            // 更新申请状态为处理中
            def form = CardReissueForm.get(it.formId)
            if (domainStateMachineHandler.canAccept(form)) {
                domainStateMachineHandler.accept(form, userId)
                form.save()
                def orderItem = new CardReissueOrderItem(form: form)
                order.addToItems(orderItem)
            }
        }

        order.save()
    }

    void update(String userId, CardReissueOrderCommand cmd) {
        CardReissueOrder order = CardReissueOrder.get(cmd.id)
        order.modifier = Teacher.load(userId)
        order.dateModified = new Date()

        cmd.addedItems.each {
            def form = CardReissueForm.get(it.formId)
            if (domainStateMachineHandler.canAccept(form)) {
                domainStateMachineHandler.accept(form, userId)
                form.save()
                def orderItem = new CardReissueOrderItem(form: form)
                order.addToItems(orderItem)
            }
        }

        cmd.removedItems.each {
            // 更新申请状态为新申请
            def form = CardReissueForm.get(it.formId)
            if (domainStateMachineHandler.canReject(form)) {
                domainStateMachineHandler.reject(form, userId)
                form.save()
                def orderItem = CardReissueOrderItem.load(it.id)
                order.removeFromItems(orderItem)
                orderItem.delete()
            }
        }

        order.save()
    }

    void delete(Long id) {
        def order = CardReissueOrder.get(id)
        if (!order) {
            throw new NotFoundException()
        }

        boolean allowStatus = order.items.every { item ->
            domainStateMachineHandler.canReject(item.form)
        }

        if (!allowStatus) {
            throw new BadRequestException()
        }

        order.items.each { item ->
            domainStateMachineHandler.canReject(item.form)
            item.form.save()
        }

        order.delete()
    }

    State receive(String userId, Long id, Long formId, boolean received) {
        def item = CardReissueOrderItem.findByOrderAndForm(CardReissueOrder.load(id), CardReissueForm.load(formId))
        if (!item) {
            throw new NotFoundException()
        }
        updateForm(userId, item, received)
    }

    State receiveAll(String userId, Long id, boolean received) {
        CardReissueOrder order = CardReissueOrder.get(id)
        order.items.each { item ->
            updateForm(userId, item, received)
        }

        received ? State.FINISHED : State.PROGRESS
    }

    State updateForm(String userId, CardReissueOrderItem item, boolean received) {
        def form = item.form
        if (received) {
            if (domainStateMachineHandler.canAccept(form)) {
                domainStateMachineHandler.accept(form, userId)
                form.save()
            }
        } else {
            if (domainStateMachineHandler.canReject(form)) {
                domainStateMachineHandler.reject(form, userId)
                form.save()
            }
        }


        form.status
    }
}
