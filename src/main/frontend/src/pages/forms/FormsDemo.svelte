<script lang="ts">
  import InputField from "$lib/components/InputField.svelte";
  import SelectField from "$lib/components/SelectField.svelte";

  let name = $state("");
  let birthDate = $state("");
  let petType = $state("");

  const petOptions = [
    { value: "dog", label: "Dog" },
    { value: "cat", label: "Cat" },
    { value: "bird", label: "Bird" },
  ];
</script>

<div class="max-w-2xl mx-auto p-8">
  <h2 class="text-2xl font-montserrat mb-6">Form Components</h2>

  <div class="bg-white p-6 rounded shadow">
    <!-- InputField: text type, error when touched+empty -->
    <InputField
      name="name"
      label="Pet Name"
      type="text"
      bind:value={name}
      error="Pet name is required"
      touched={true}
    />

    <!-- InputField: date type -->
    <InputField
      name="birthDate"
      label="Birth Date"
      type="date"
      bind:value={birthDate}
      touched={false}
    />

    <!-- SelectField: options with error state -->
    <SelectField
      name="petType"
      label="Pet Type"
      options={petOptions}
      bind:value={petType}
      error="Pet type is required"
      touched={true}
    />
  </div>
</div>
