<template>
    <bk-checkbox-group :value="paramValue[paramKey]" v-if="isEdit" @change="changeParamValue" v-bind="$props" class="g-accelerate-single-width">
        <bk-checkbox :value="value" v-for="(key, value) in paramEnum" :key="key" class="g-accelerate-mr-10">
            {{ key }}
        </bk-checkbox>
    </bk-checkbox-group>
    <span v-else>{{ displayValue }}</span>
</template>

<script>
    import mixin from './mixins.js'

    export default {
        mixins: [mixin]
    }
</script>
