import React from "react";

const roles = [
  { value: "submitter", label: "Submitter", icon: "👤", description: "Share ideas" },
  { value: "evaluator/admin", label: "Evaluator", icon: "🔍", description: "Review ideas" }
];

export default function RoleSelector({ value, onChange, error }) {
  return (
    <div>
      <label className="block text-sm font-medium text-slate-700 mb-3">
        Select your role
      </label>
      <div className="grid grid-cols-2 gap-3">
        {roles.map((role) => (
          <button
            key={role.value}
            type="button"
            onClick={() => onChange(role.value)}
            className={`relative flex flex-col items-center gap-2 rounded-xl border-2 p-4 transition-all ${
              value === role.value
                ? 'border-blue-600 bg-blue-50'
                : 'border-slate-200 bg-white hover:border-blue-300'
            }`}
          >
            <div className="text-3xl">{role.icon}</div>
            <div className="text-center">
              <p className={`text-sm font-semibold ${
                value === role.value ? 'text-blue-700' : 'text-slate-900'
              }`}>
                {role.label}
              </p>
              <p className="text-xs text-slate-500 mt-0.5">{role.description}</p>
            </div>
            {value === role.value && (
              <div className="absolute top-2 right-2">
                <svg className="h-5 w-5 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                </svg>
              </div>
            )}
          </button>
        ))}
      </div>
      {error && <p className="mt-2 text-sm text-red-600 font-medium">{error}</p>}
    </div>
  );
}


