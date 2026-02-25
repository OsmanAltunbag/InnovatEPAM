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
        className="mt-1.5 w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
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
