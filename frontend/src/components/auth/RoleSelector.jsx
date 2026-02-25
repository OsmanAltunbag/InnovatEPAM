import React from "react";

const roles = [
  { value: "submitter", label: "Submitter" },
  { value: "evaluator/admin", label: "Evaluator/Admin" }
];

export default function RoleSelector({ value, onChange, error }) {
  return (
    <div>
      <label htmlFor="role" className="block text-sm font-medium text-slate-700">Role</label>
      <select
        id="role"
        className="mt-1 w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-slate-900"
        value={value}
        onChange={(event) => onChange(event.target.value)}
      >
        <option value="">Select role</option>
        {roles.map((role) => (
          <option key={role.value} value={role.value}>
            {role.label}
          </option>
        ))}
      </select>
      {error ? <p className="mt-1 text-sm text-red-600">{error}</p> : null}
    </div>
  );
}
