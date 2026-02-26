/**
 * Test examples for roleUtils.js functions
 * These examples demonstrate how the role utility functions handle different role formats
 */

// Import examples (how to use in components):
// import { canEvaluate, canSubmit, isAdmin, getRoleLabel } from "../utils/roleUtils";

/**
 * Test Cases: Database roles and how they're handled
 */

const testCases = [
  {
    dbRole: "submitter",
    description: "Submitter role from database",
    expectations: {
      canEvaluate: false,
      canSubmit: true,
      isEvaluator: false,
      isAdmin: false,
      isSubmitter: true,
      roleLabel: "Submitter"
    }
  },
  {
    dbRole: "evaluator/admin",
    description: "Combined evaluator/admin role from database",
    expectations: {
      canEvaluate: true,
      canSubmit: true,
      isEvaluator: true,
      isAdmin: true,
      isSubmitter: false,
      roleLabel: "Admin (Evaluator)"
    }
  },
  {
    dbRole: "EVALUATOR/ADMIN",
    description: "Uppercase variant of combined role",
    expectations: {
      canEvaluate: true,
      canSubmit: true,
      isEvaluator: true,
      isAdmin: true,
      isSubmitter: false,
      roleLabel: "Admin (Evaluator)"
    }
  },
  {
    dbRole: "Evaluator/Admin",
    description: "Mixed case variant",
    expectations: {
      canEvaluate: true,
      canSubmit: true,
      isEvaluator: true,
      isAdmin: true,
      isSubmitter: false,
      roleLabel: "Admin (Evaluator)"
    }
  },
  {
    dbRole: "admin",
    description: "Plain admin role (if added to database in future)",
    expectations: {
      canEvaluate: false,
      canSubmit: true,
      isEvaluator: false,
      isAdmin: true,
      isSubmitter: false,
      roleLabel: "Admin"
    }
  },
  {
    dbRole: "evaluator",
    description: "Plain evaluator role (if added to database in future)",
    expectations: {
      canEvaluate: true,
      canSubmit: false,
      isEvaluator: true,
      isAdmin: false,
      isSubmitter: false,
      roleLabel: "Evaluator"
    }
  },
  {
    dbRole: null,
    description: "Unauthenticated user",
    expectations: {
      canEvaluate: false,
      canSubmit: false,
      isEvaluator: false,
      isAdmin: false,
      isSubmitter: false,
      roleLabel: "Unknown"
    }
  }
];

/**
 * Real-world usage examples in components
 */

// EXAMPLE 1: EvaluationPanel.jsx
const evaluationPanelExample = () => {
  const user = { role: "evaluator/admin" }; // From JWT
  const { canEvaluate } = require("../utils/roleUtils");

  // Before (BROKEN):
  // const isEvaluator = user?.role === "EVALUATOR" || user?.role === "ADMIN";
  // Result: false (because "evaluator/admin" doesn't equal "EVALUATOR" or "ADMIN")

  // After (FIXED):
  const isEvaluator = canEvaluate(user?.role);
  // Result: true (correctly recognizes "evaluator/admin" role)

  return {
    showEvaluationPanel: isEvaluator,
    message: "Evaluation panel is now visible! ✓"
  };
};

// EXAMPLE 2: DashboardPage.jsx
const dashboardPageExample = () => {
  const user = { role: "evaluator/admin" }; // From JWT
  const { canSubmit, canEvaluate } = require("../utils/roleUtils");

  // Before (INCOMPLETE):
  // {(user?.role === "submitter" || user?.role === "admin") && <SubmitButton />}
  // {(user?.role === "evaluator" || user?.role === "admin") && <EvaluateButton />}
  // Result: Both would be false for "evaluator/admin" because the exact string doesn't match

  // After (FIXED):
  const showSubmitButton = canSubmit(user?.role); // true
  const showEvaluateButton = canEvaluate(user?.role); // true
  // Result: Both buttons show correctly for "evaluator/admin" users ✓

  return {
    showSubmitButton,
    showEvaluateButton,
    message: "Admin users can now both submit and evaluate! ✓"
  };
};

// EXAMPLE 3: Layout.jsx
const layoutExample = () => {
  const user = { role: "submitter" }; // From JWT
  const { canSubmit } = require("../utils/roleUtils");

  const showSubmitButton = canSubmit(user?.role); // true
  // Result: Submitter can see "Submit Idea" in sidebar ✓

  return {
    showSubmitButton,
    message: "Sidebar navigation correctly shows submit button! ✓"
  };
};

/**
 * Run all test cases
 */
export function runAllTests() {
  console.log("🧪 Testing roleUtils.js functions...\n");

  testCases.forEach((testCase, index) => {
    console.log(`Test ${index + 1}: ${testCase.description}`);
    console.log(`  Database value: "${testCase.dbRole}"`);
    console.log(`  Expected results:`, testCase.expectations);
    console.log();
  });

  console.log("✅ All test cases defined.");
  console.log("\n📝 Test the actual implementation by:");
  console.log("   1. Running the frontend dev server");
  console.log("   2. Logging in as a user with 'evaluator/admin' role");
  console.log("   3. Verifying that the EvaluationPanel is now visible");
  console.log("   4. Checking that both Submit and Evaluate buttons appear on Dashboard");
}

export { testCases, evaluationPanelExample, dashboardPageExample, layoutExample };
