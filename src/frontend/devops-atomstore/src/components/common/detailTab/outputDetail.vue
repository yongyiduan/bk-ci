<template>
    <section>
        <div class="explain">{{ $t('store.说明：') }}<span class="explain-info">{{ $t('store.说明文案') }}</span></div>
        <bk-table :data="outputData" v-bkloading="{ isLoading: isLoading }">
            <bk-table-column :label="$t('store.参数名')" prop="name" show-overflow-tooltip></bk-table-column>
            <bk-table-column :label="$t('store.参数说明')" prop="desc" show-overflow-tooltip></bk-table-column>
        </bk-table>
    </section>
</template>

<script>
    import api from '@/api'

    export default {
        data () {
            return {
                outputData: [],
                isLoading: false
            }
        },

        created () {
            this.initOutputData()
        },

        methods: {
            initOutputData () {
                const code = this.$route.params.code
                this.isLoading = true
                return api.requestAtomOutputList(code).then((res) => {
                    this.outputData = res
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isLoading = false
                })
            }
        }
    }
</script>

<style lang="scss" scoped>
    .explain {
        margin: 20px 0 10px;
        color: #707070;
        font-weight: 700;
        font-size: 12px;
        .explain-info {
            font-weight: 400;
            color: #a7a7a7;
        }
    }
    .yaml-title {
        margin: 20px 0 10px;
        line-height: 23px;
        height: 23px;
        color: #222222;
        font-size: 14px;
        font-weight: 500;
    }
</style>
