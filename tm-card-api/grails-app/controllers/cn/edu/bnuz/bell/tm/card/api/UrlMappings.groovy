package cn.edu.bnuz.bell.tm.card.api

class UrlMappings {

    static mappings = {
        "/students"(resources: 'student', includes: []) {
            "/reissues"(resources: 'reissueForm') {
                "/approvers"(controller: 'reissueForm', action: 'approvers', method: 'GET')
                collection {
                    "/notice"(controller: 'reissueForm', action: 'notice', method: 'GET')
                }
            }
        }

        "/approvers"(resources: 'approver', includes: []) {
            "/reissues"(resources: 'reissueApproval', includes:['index']) {
                "/workitems"(resources: 'reissueApproval', includes: ['show', 'patch'])
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
