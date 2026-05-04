<script lang="ts">
  let {
    name,
    label,
    type = "text",
    value = "",
    error = "",
    touched = false,
  }: {
    name: string;
    label: string;
    type?: "text" | "date";
    value?: string;
    error?: string;
    touched?: boolean;
    dirty?: boolean;
  } = $props();

  let showError = $derived(touched && error !== "");
  let inputClass = $derived(
    "block w-full rounded border px-3 py-1.5 text-base font-normal leading-normal focus:outline-none focus:ring-2 focus:ring-offset-0 " +
      (showError
        ? "border-red-500 focus:border-red-500 focus:ring-red-200"
        : "border-gray-300 focus:border-blue-400 focus:ring-blue-200")
  );
</script>

<div class="form-group mb-4">
  <label for={name} class="control-label mb-1 block font-medium text-gray-700 text-sm">
    {label}
  </label>
  <input {type} {name} id={name} bind:value class={inputClass} />
  {#if showError}
    <span class="help-inline mt-1 block text-sm text-red-600">{error}</span>
  {/if}
</div>
