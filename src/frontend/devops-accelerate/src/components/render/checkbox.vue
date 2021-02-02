<template>
    <bk-checkbox-group :value="paramValue[paramKey]" v-if="isEdit" @change="changeParamValue" v-bind="$props" class="g-accelerate-single-width">
        <bk-checkbox :value="param.id" v-for="param in paramEnum" :key="param.id">
            {{ param.name }}
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
