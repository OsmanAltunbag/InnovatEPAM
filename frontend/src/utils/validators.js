const emailRegex = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;

export const normalizeEmail = (value) => value.trim().toLowerCase();

export const isValidEmail = (value) => emailRegex.test(normalizeEmail(value));

export const isStrongPassword = (value) => {
  if (!value) {
    return false;
  }
  return value.trim().length >= 8;
};
