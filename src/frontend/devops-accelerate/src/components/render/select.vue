<template>
    <bk-select :value="paramValue[paramKey]" v-if="isEdit" @change="changeParamValue" v-bind="$props" class="g-accelerate-single-width">
        <bk-option v-for="(key, value) in paramEnum"
            :key="key"
            :id="value"
            :name="key">
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
