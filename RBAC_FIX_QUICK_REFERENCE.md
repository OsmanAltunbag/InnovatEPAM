# RBAC Bug Fix - Quick Reference Guide

## 🎯 Problem Fixed
Admin/Evaluator users could not see the evaluation panel because role checks used hardcoded strings that didn't match the database role values.

## ✅ Solution Summary

### Files Created
1. **`frontend/src/utils/roleUtils.js`** - New utility module with 6 flexible role-checking functions

### Files Modified
1. **`frontend/src/components/EvaluationPanel.jsx`**
   - Changed: Line 35 from hardcoded check to `canEvaluate(user?.role)`
   
2. **`frontend/src/pages/DashboardPage.jsx`**
   - Changed: Two hardcoded checks to `canSubmit()` and `canEvaluate()`
   
3. **`frontend/src/components/Layout.jsx`**
   - Changed: One hardcoded check to `canSubmit(user?.role)`

### No Backend Changes Needed
- JwtService.java already works correctly
- Database roles unchanged

## 🔧 The Solution: Role Utility Functions

### Core Functions (in `roleUtils.js`):

```javascript
// Check if user can evaluate ideas
canEvaluate(user?.role)        // true if role includes "evaluator" OR "admin"

// Check if user can submit ideas  
canSubmit(user?.role)          // true if role is "submitter" OR includes "admin"

// Individual role checks (case-insensitive)
isAdmin(user?.role)            // true for "admin", "ADMIN", "Admin", "evaluator/admin"
isEvaluator(user?.role)        // true for "evaluator", "Evaluator", "evaluator/admin"
isSubmitter(user?.role)        // true for "submitter", "SUBMITTER"

// Get human-readable role label
getRoleLabel(user?.role)       // "Admin (Evaluator)", "Evaluator", "Submitter", etc.
```

## 📊 Before vs After

### BEFORE (Broken):
```jsx
// EvaluationPanel.jsx - WRONG: "EVALUATOR" doesn't match "evaluator/admin" from DB
const isEvaluator = user?.role === "EVALUATOR" || user?.role === "ADMIN";
// Result for "evaluator/admin" user: false ❌

// DashboardPage.jsx - INCOMPLETE: Doesn't handle "evaluator/admin" role
{(user?.role === "submitter" || user?.role === "admin") && <SubmitButton />}
// Result for "evaluator/admin" user: false ❌
```

### AFTER (Fixed):
```jsx
// EvaluationPanel.jsx - RIGHT: Works with any role format
const isEvaluator = canEvaluate(user?.role);
// Result for "evaluator/admin" user: true ✓

// DashboardPage.jsx - COMPLETE: Handles combined "evaluator/admin" role
{canSubmit(user?.role) && <SubmitButton />}
// Result for "evaluator/admin" user: true ✓
```

## 📋 Role Handling Matrix

| Database Role | canEvaluate() | canSubmit() | isAdmin() | isEvaluator() | isSubmitter() |
|---|---|---|---|---|---|
| `submitter` | false | true | false | false | true |
| `evaluator/admin` | **true** | **true** | **true** | **true** | false |
| `evaluator` (future) | true | false | false | true | false |
| `admin` (future) | false | true | true | false | false |
| `SUBMITTER` (uppercase) | false | true | false | false | true |
| `Evaluator/Admin` (mixed) | true | true | true | true | false |

## 🚀 How to Test

### 1. Frontend Dev Server
```bash
cd frontend
npm run dev
```

### 2. Login with Test User
- Create a user with role `evaluator/admin` in the database
- Or verify an existing admin user

### 3. Verify the Fix
- [ ] EvaluationPanel is now visible (Admin can see it)
- [ ] Dashboard shows "Evaluate Ideas" button for Admin
- [ ] Dashboard shows "Submit Idea" button for Admin
- [ ] Layout sidebar shows "Submit Idea" option for Admin
- [ ] All buttons work correctly (no errors in browser console)

## 💡 Why This Fix Works

1. **Case-Insensitive**: Uses `.toLowerCase()` so "EVALUATOR", "Evaluator", "evaluator" all work
2. **Format-Agnostic**: Uses `.includes()` so "evaluator/admin" is recognized as both roles
3. **Combined Roles**: Properly handles "evaluator/admin" where one role has multiple privileges
4. **Centralized**: All role checks use the same functions (DRY principle)
5. **Maintainable**: Easy to add new roles or role combinations without changing components

## 📝 JWT Token Structure

The JWT token contains:
```json
{
  "sub": "user@example.com",
  "userId": "uuid-here",
  "role": "evaluator/admin",    // <-- This comes from database
  "createdAt": "2026-02-26T..."
}
```

The role value is taken directly from the `roles` table `name` column:
- Database: `roles.name = 'evaluator/admin'`
- JWT: `claims.put("role", user.getRole().getName())` → `"evaluator/admin"`
- Frontend: Decoded JWT has `role: "evaluator/admin"`
- Now correctly handled by `canEvaluate("evaluator/admin")` → `true` ✓

## 🐛 Previously Failing Scenarios

✅ Fixed: Admin users ("evaluator/admin") can now evaluate ideas
✅ Fixed: Admin users can see the EvaluationPanel
✅ Fixed: Admin users can see both Submit and Evaluate buttons
✅ Fixed: Case-sensitivity issues (uppercase, mixed case)
✅ Fixed: Combined role handling ("evaluator/admin" as single role)

## 📚 Additional Resources

- See `RBAC_FIX_DOCUMENTATION.md` for detailed technical explanation
- See `RBAC_FIX_TEST_EXAMPLES.js` for test case examples
- Source: `frontend/src/utils/roleUtils.js` for implementation details

## ⚠️ Important Notes

- No database migrations needed
- No backend code changes needed
- All changes are frontend-only
- Backward compatible with existing role formats
- Ready for future role additions (just add logic to `roleUtils.js`)
