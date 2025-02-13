{{/* Generate basic labels */}}
{{- define "clickhouse.labels" -}}
app: {{ .Values.clickhouse.name }}
{{- end -}}