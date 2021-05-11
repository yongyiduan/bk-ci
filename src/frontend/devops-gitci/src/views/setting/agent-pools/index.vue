<template>
    <article class="agent-pools" @scroll.passive="mainScroll">
        <h3 :class="{ 'pool-title': true, 'fix-top': scrollTop > 60 && scrollTop < 1030 }">Default agent pools</h3>
        <section class="agent-pools-container">
            <agent-pool-card class="agent-pool" :editable="false"></agent-pool-card>
        </section>

        <h3 :class="{ 'pool-title': true, 'fix-top': scrollTop > 1030 }">
            Self-hosted agent pools
            <bk-button @click="showAddPool" class="add-pool" theme="primary" size="small">Add Pool</bk-button>
        </h3>
        <section class="agent-pools-container">
            <agent-pool-card class="agent-pool"></agent-pool-card>
        </section>

        <add-pool :show.sync="isShowAddPool"></add-pool>
    </article>
</template>

<script>
    import agentPoolCard from '@/components/setting/agent-pool-card'
    import addPool from '@/components/setting/add-pool'

    export default {
        components: {
            agentPoolCard,
            addPool
        },

        data () {
            return {
                isShowAddPool: false,
                scrollTop: 0
            }
        },

        methods: {
            showAddPool () {
                this.isShowAddPool = true
            },

            mainScroll (event) {
                this.scrollTop = event.target.scrollTop
            }
        }
    }
</script>

<style lang="postcss" scoped>
    .agent-pools {
        overflow-y: auto;
        height: calc(100vh - 60px);
        padding: 0 24px 20px;
        margin: 0;
        width: 100%;
    }
    .pool-title {
        margin-top: 20px;
        color: #313328;
        font-size: 16px;
        line-height: 21px;
        width: 100%;
        .add-pool {
            margin-left: 30px;
        }
        &.fix-top {
            position: fixed;
            top: 60px;
            z-index: 2;
            background: #fff;
            line-height: 48px;
            height: 48px;
            box-shadow: 0 2px 5px 0 rgba(51,60,72,0.03);
            margin: 0 -24px;
            padding: 0 24px;
        }
    }
    .agent-pools-container {
        &:after {
            content: '';
            display: table;
            clear: both;
        }
        .agent-pool {
            float: left;
            margin-top: 20px;
            &:not(:nth-child(4n + 1)) {
                margin-left: 20px;
            }
        }
    }
</style>
