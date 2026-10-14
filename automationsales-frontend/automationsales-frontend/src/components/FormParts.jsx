// Small pieces used by every form.
import { niceLabel } from '../utils';

// A label + input + error message under it.
//   <Field label="Email" error={error?.fieldErrors?.email}>
//     <input name="email" ... />
//   </Field>
export function Field({ label, error, fullWidth = false, children }) {
  return (
    <label className={fullWidth ? 'field full-width' : 'field'}>
      {label}
      {children}
      {error && <small className="field-error">{error}</small>}
    </label>
  );
}

// The red box at the top of a form showing the backend's error message.
export function FormError({ error }) {
  if (!error) return null;
  return (
    <div className="form-error">
      {error.fieldErrors ? 'Please fix the highlighted fields.' : error.message}
    </div>
  );
}

// <option> tags for a list of enum values: <EnumOptions values={['NEW', 'LOST']} />
export function EnumOptions({ values, emptyText }) {
  return (
    <>
      {emptyText && <option value="">{emptyText}</option>}
      {values.map((value) => (
        <option key={value} value={value}>{niceLabel(value)}</option>
      ))}
    </>
  );
}

// Loading and error messages for whole sections
export function Loading() {
  return <div className="loading">Loading…</div>;
}

export function ErrorBox({ error }) {
  return <div className="error-box">Could not load data: {error.message}</div>;
}
