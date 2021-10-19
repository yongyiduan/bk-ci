<template>
    <section>
        <h3 class="yaml-title">{{ $t('store.质量红线指标') }}:</h3>
        <bk-table :data="qualityData" v-bkloading="{ isLoading }">
            <bk-table-column :label="$t('store.指标名')" prop="enName" show-overflow-tooltip></bk-table-column>
            <bk-table-column :label="$t('store.参数说明')" prop="desc" show-overflow-tooltip></bk-table-column>
            <bk-table-column :label="$t('store.值类型')" prop="thresholdType" show-overflow-tooltip></bk-table-column>
            <bk-table-column :label="$t('store.支持的操作')" prop="operationList" show-overflow-tooltip :formatter="operationFormatter"></bk-table-column>
        </bk-table>
    </section>
</template>

<script>
    export default {
        props: {
            qualityData: {
                type: Array,
                default: () => []
            }
        },

        methods: {
            operationFormatter (row, column, cellValue, index) {
                const opeMap = {
                    GT: '>',
                    GE: '>=',
                    LT: '<',
                    LE: '<=',
                    EQ: '='
                }
                return (cellValue || []).reduce((acc, cur) => {
                    acc += (opeMap[cur] + ' ')
                    return acc
                }, '')
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
