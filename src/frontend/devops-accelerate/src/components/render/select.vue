<template>
    <bk-select :value="paramValue[paramKey]" @change="changeParamValue" v-bind="$props" class="g-accelerate-single-width">
        <bk-option v-for="(param, index) in paramEnum"
            :key="index"
            :id="param.paramValue"
            :name="param.paramName">
        </bk-option>
    </bk-select>
</template>

<script>
    import mixin from './mixins.js'

    export default {
        mixins: [mixin]
    }
</script>
