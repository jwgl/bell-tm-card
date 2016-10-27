package cn.edu.bnuz.bell.card

class CardReissueOrderCommand {
    Long id

    List<OrderItem> addedItems

    List<OrderItem> removedItems

    class OrderItem {
        Long id
        Long formId
    }
}
