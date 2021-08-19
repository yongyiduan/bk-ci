<template>
    <section>
        <span class="review-subtitle">Custom Variables</span>

        <bk-table :data="params">
            <bk-table-column label="ChineseName" prop="chineseName" :formatter="chineseNameFormatter" show-overflow-tooltip></bk-table-column>
            <bk-table-column label="Name" prop="key" :formatter="nameFormatter" show-overflow-tooltip></bk-table-column>
            <bk-table-column label="Value" prop="value" :formatter="valFormatter" show-overflow-tooltip></bk-table-column>
        </bk-table>
    </section>
</template>

<script>
    export default {
        props: {
            params: Array
        },

        methods: {
            chineseNameFormatter (row, column, cellValue, index) {
                return cellValue || '--'
            },

            nameFormatter (row, column, cellValue, index) {
                return (cellValue || '').replace(/^variables\./, '')
            },

            valFormatter (row, column, cellValue, index) {
                let res = cellValue || '--'
                if (Array.isArray(cellValue)) {
                    res = `[${cellValue.join(', ')}]`
                } else {
                    res = String(cellValue) || '--'
                }
                return res
            }
        }
    }
</script>
