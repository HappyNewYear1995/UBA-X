<template>
  <div class="page">
    <h2 class="page-title">参数设置</h2>
    <p class="page-sub">平台全局运行参数，修改后即时生效（清理任务、采集开关等依赖这些配置）</p>

    <div class="toolbar">
      <el-button type="primary" :icon="Check" :loading="saving" @click="save">保存修改</el-button>
      <el-button :icon="Refresh" @click="load">重置</el-button>
      <span class="muted" style="margin-left:auto">共 {{ itemCount }} 项配置</span>
    </div>

    <div v-for="g in groups" :key="g.group" class="card">
      <div class="card-title">{{ g.group }}</div>
      <el-form label-width="180px" class="cfg-form">
        <el-form-item v-for="c in g.items" :key="c.configKey" :label="c.configName">
          <el-switch
            v-if="c.valueType === 'BOOLEAN'"
            v-model="edited[c.configKey]"
            active-value="true" inactive-value="false" />
          <el-input v-else v-model="edited[c.configKey]" :style="{ width: c.valueType === 'NUMBER' ? '160px' : '320px' }" />
          <span class="cfg-key mono">{{ c.configKey }}</span>
          <span v-if="c.remark" class="cfg-remark">{{ c.remark }}</span>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { Check, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { systemApi } from '../../api'

const groups = ref([])
const edited = reactive({})
const saving = ref(false)

const itemCount = computed(() => groups.value.reduce((n, g) => n + g.items.length, 0))

async function load() {
  groups.value = await systemApi.config()
  for (const g of groups.value) for (const c of g.items) edited[c.configKey] = c.configValue
}

async function save() {
  const edits = []
  for (const g of groups.value) {
    for (const c of g.items) {
      if (String(edited[c.configKey]) !== String(c.configValue)) {
        edits.push({ configKey: c.configKey, configValue: String(edited[c.configKey]) })
      }
    }
  }
  if (!edits.length) { ElMessage.info('没有需要保存的修改'); return }
  saving.value = true
  try {
    await systemApi.saveConfig(edits)
    ElMessage.success('已保存 ' + edits.length + ' 项配置')
    await load()
  } finally { saving.value = false }
}

onMounted(load)
</script>

<style scoped>
.cfg-form :deep(.el-form-item) { margin-bottom: 14px; }
.cfg-key { margin-left: 12px; font-size: 12px; color: #b0b6c0; }
.cfg-remark { margin-left: 12px; font-size: 12px; color: #9aa2af; }
</style>
