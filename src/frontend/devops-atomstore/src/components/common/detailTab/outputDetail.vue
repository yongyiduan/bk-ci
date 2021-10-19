<template>
    <section>
        <h3 class="yaml-title">{{ $t('store.输出参数') }}:</h3>
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
    .yaml-title {
        margin: 20px 0 10px;
        line-height: 23px;
        height: 23px;
        color: #222222;
        font-size: 14px;
        font-weight: 500;
    }
</style>
