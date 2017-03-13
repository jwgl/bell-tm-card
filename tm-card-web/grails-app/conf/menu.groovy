menuGroup 'main', {
    affair 40, {
        cardReissueForm     40, 'PERM_CARD_REISSUE_WRITE',   '/web/card/students/${userId}/reissues'
        cardReissueApproval 41, 'PERM_CARD_REISSUE_APPROVE', '/web/card/approvers/${userId}/reissues'
        cardReissueOrder    42, 'PERM_CARD_REISSUE_APPROVE', '/web/card/reissueOrders'
    }
}