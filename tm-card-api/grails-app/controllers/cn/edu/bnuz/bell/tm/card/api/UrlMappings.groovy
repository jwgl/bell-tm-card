package cn.edu.bnuz.bell.tm.card.api

class UrlMappings {

    static mappings = {
        "/students"(resources: 'student', includes: []) {
            "/reissues"(resources: 'reissueForm') {
                "/checkers"(controller: 'reissueForm', action: 'checkers', method: 'GET')
            }
        }

        "/reviewers"(resources: 'reviewer', includes: []) {
            "/reissues"(resources: 'reissueReview', includes:['index']) {
                "/workitems"(resources: 'reissueReview', includes: ['show', 'patch'])
            }
        }

        "/reissues"(resources: 'reissue', includes: ['index', 'show'])

        "/reissueOrders"(resources: 'reissueOrder')

        "500"(view: '/error')
        "404"(view: '/notFound')
        "403"(view: '/forbidden')
        "401"(view: '/unauthorized')
    }
}
