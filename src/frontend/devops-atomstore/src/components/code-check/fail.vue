<template>
    <section class="code-ckeck-status">
        <i class="bk-icon icon-exclamation-circle status-icon"></i>
        <section class="code-check-summary">
            <h3 class="summary-head">代码质量不合格</h3>
            <h5 class="summary-desc">代码安全、代码规范、代码度量评分均 &gt;= 90 为合格，其中任一项 &lt; 90 即不合格</h5>
            <h5 class="summary-link">最近检查:<span class="link-txt">scscs</span>2019-02-15 14:32:49 <span class="link-txt">查看详情</span></h5>
        </section>
        <bk-button theme="primary" class="code-check-button">查看详情</bk-button>
    </section>
</template>

<script>
    export default {
        
    }
</script>

<style lang="scss" scoped>
    .icon-exclamation-circle {
        color: #ea3636;
    }
    .code-ckeck-status {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        padding: 3.2vh;
        border-bottom: 1px solid #f0f1f5;
        .status-icon {
            font-size: 42px;
            margin-right: 21px;
        }
        .code-check-summary {
            flex: 1;
            color: #63656e;
            font-size: 12px;
            line-height: 17px;
            .summary-head {
                color: #313238;
                line-height: 28px;
                font-size: 20px;
                font-weight: normal;
            }
            .summary-desc {
                display: inline-block;
                margin: 3px 0 5px;
                font-weight: normal;
            }
            .summary-link {
                opacity: 0.9;
                font-weight: normal;
                line-height: 16px;
            }
            .link-txt {
                color: #3a84ff;
                cursor: pointer;
                margin: 0 8px;
            }
        }
        .code-check-button {
            margin-top: 10px;
        }
    }
</style>
