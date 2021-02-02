<template>
    <bk-select :value="paramValue[paramKey]" v-if="isEdit" @change="changeParamValue" v-bind="$props" class="g-accelerate-single-width">
        <bk-option v-for="option in paramEnum"
            :key="option.id"
            :id="option.id"
            :name="option.name">
        </bk-option>
    </bk-select>
    <span v-else>{{ displayValue }}</span>
</template>

<script>
    import mixin from './mixins.js'

    export default {
        mixins: [mixin]
    }
</script>
