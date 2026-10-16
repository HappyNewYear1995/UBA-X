<template>
  <div class="page">
    <h2 class="page-title">数据字典</h2>
    <p class="page-sub">维护系统内枚举与标签映射（事件类型、渠道来源、设备类型等）</p>

    <div class="dict-wrap">
      <div class="card dict-side">
        <div class="card-title">字典类型</div>
        <div
          v-for="t in types" :key="t"
          class="type-item" :class="{ active: t === current }"
          @click="selectType(t)"
        >
          <el-icon><Collection /></el-icon><span>{{ t }}</span>
          <span class="cnt">{{ countOf(t) }}</span>
        </div>
      </div>

      <div class="card dict-main">
        <div class="card-title">
          <span>{{ current || '请选择类型' }} · 字典项</span>
          <el-button type="primary" size="small" :icon="Plus" :disabled="!current" @click="openCreate">新增</el-button>
        </div>
        <el-table :data="rows" size="small" stripe>
          <el-table-column prop="dictLabel" label="标签" min-width="140" />
          <el-table-column prop="dictValue" label="键值" min-width="140" />
          <el-table-column prop="sortNo" label="排序" width="80" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="row.status === 'ENABLED' ? 'success' : 'info'">{{ row.status === 'ENABLED' ? '启用' : '停用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
          <el-table-column label="操作" width="130" fixed="right">
            <template #default="{ row }">
              <el-button link size="small" @click="openEdit(row)">编辑</el-button>
              <el-button link type="danger" size="small" @click="remove(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog v-model="visible" :title="editing ? '编辑字典项' : '新增字典项'" width="440px">
      <el-form :model="form" label-width="70px">
        <el-form-item label="类型"><el-input v-model="form.dictType" :disabled="editing" /></el-form-item>
        <el-form-item label="标签"><el-input v-model="form.dictLabel" /></el-form-item>
        <el-form-item label="键值"><el-input v-model="form.dictValue" :disabled="editing" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortNo" :min="0" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width:100%">
            <el-option label="启用" value="ENABLED" /><el-option label="停用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { Plus, Collection } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { systemApi } from '../../api'

const types = ref([])
const current = ref('')
const rows = ref([])
const allByType = reactive({})
const visible = ref(false)
const editing = ref(false)
const form = reactive({ id: null, dictType: '', dictLabel: '', dictValue: '', sortNo: 0, status: 'ENABLED', remark: '' })

function countOf(t) { return (allByType[t] || []).length }

async function loadTypes() {
  types.value = await systemApi.dictTypes()
  for (const t of types.value) allByType[t] = await systemApi.dict(t)
  if (types.value.length && !current.value) selectType(types.value[0])
}
function selectType(t) { current.value = t; rows.value = allByType[t] || [] }

function openCreate() {
  editing.value = false
  Object.assign(form, { id: null, dictType: current.value, dictLabel: '', dictValue: '', sortNo: rows.value.length + 1, status: 'ENABLED', remark: '' })
  visible.value = true
}
function openEdit(row) { editing.value = true; Object.assign(form, row); visible.value = true }

async function save() {
  if (!form.dictLabel || !form.dictValue) { ElMessage.warning('标签与键值必填'); return }
  await systemApi.saveDict({ ...form })
  ElMessage.success('已保存'); visible.value = false
  await refreshCurrent()
}
async function remove(row) {
  try {
    await ElMessageBox.confirm(`确认删除「${row.dictLabel}」？`, '提示', { type: 'warning' })
  } catch (e) { return }
  await systemApi.removeDict(row.id); ElMessage.success('已删除'); await refreshCurrent()
}
async function refreshCurrent() {
  const t = current.value
  types.value = await systemApi.dictTypes()
  for (const ty of types.value) allByType[ty] = await systemApi.dict(ty)
  if (!types.value.includes(t)) t = types.value[0]
  selectType(t)
}

onMounted(loadTypes)
</script>

<style scoped>
.dict-wrap { display: grid; grid-template-columns: 220px 1fr; gap: 16px; align-items: start; }
.dict-side { margin-top: 0; }
.dict-main { margin-top: 0; }
.type-item {
  display: flex; align-items: center; gap: 8px; padding: 9px 10px; border-radius: 8px;
  cursor: pointer; font-size: 13.5px; color: #4a5262; margin-top: 2px;
}
.type-item:hover { background: #f2f6ff; }
.type-item.active { background: #e8efff; color: #2b5cff; font-weight: 600; }
.type-item .cnt { margin-left: auto; font-size: 12px; color: #9aa2af; }
@media (max-width: 900px) { .dict-wrap { grid-template-columns: 1fr; } }
</style>
