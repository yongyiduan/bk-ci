export default {
    props: {
        paramEnum: Object,
        paramKey: String,
        isEdit: Boolean,
        paramValue: Object
    },

    computed: {
        displayValue () {
            const value = this.paramValue[this.paramKey]
            const keys = Object.keys(this.paramEnum) || []
            const curKey = keys.find((key) => (+this.paramEnum[key] === +value)) || ''
            return curKey
        }
    },

    methods: {
        changeParamValue (val) {
            const paramValue = JSON.parse(JSON.stringify(this.paramValue))
            paramValue[this.paramKey] = val
            this.$emit('update:paramValue', paramValue)
        }
    }
}
