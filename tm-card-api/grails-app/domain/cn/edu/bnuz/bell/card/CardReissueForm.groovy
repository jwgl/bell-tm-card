package cn.edu.bnuz.bell.card

import cn.edu.bnuz.bell.organization.Student
import cn.edu.bnuz.bell.workflow.IStateObject
import cn.edu.bnuz.bell.workflow.States
import cn.edu.bnuz.bell.workflow.WorkflowInstance

/**
 * 学生证补办申请表
 * @author Yang Lin
 */
class CardReissueForm implements IStateObject {
    /**
     * 申请人
     */
    Student student

    /**
     * 事由
     */
    String reason

    /**
     * 创建时间
     */
    Date dateCreated

    /**
     * 修改时间
     */
    Date dateModified

    /**
     * 状态，见CardReissureStateMachineConfiguration
     */
    States status

    /**
     * 工作流实例
     */
    WorkflowInstance workflowInstance

    static mapping = {
        comment          '补办学生证申请表'
        id               generator: 'identity', comment: '补办学生证申请ID'
        student          comment: '申请人'
        reason           length: 255, comment: '事由'
        dateCreated      comment: '创建时间'
        dateModified     comment: '修改时间'
        status           comment: '状态：0-新建，1-等审核，3-退回，4-通过，7-制作中，8-完成'
        workflowInstance comment: '工作流实例'
    }

    static constraints = {
        workflowInstance nullable: true
    }

    String getWorkflowId() {
        'card.reissue'
    }
}
