menuGroup 'main', {
    affair 40, {
        cardReissueForm   40, 'PERM_CARD_REISSUE_WRITE',  '/web/card/students/${userId}/reissues'
        cardReissueReview 41, 'PERM_CARD_REISSUE_CHECK',  '/web/card/reviewers/${userId}/reissues'
        cardReissueOrder  42, 'PERM_CARD_REISSUE_CHECK',  '/web/card/reissueOrders'
    }
}