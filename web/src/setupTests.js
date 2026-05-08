// jest-dom adds custom matchers for asserting on DOM nodes
import '@testing-library/jest-dom';

// Polyfill TextEncoder/TextDecoder for jsdom (required by react-router-dom v7)
const { TextEncoder, TextDecoder } = require('util');
global.TextEncoder = TextEncoder;
global.TextDecoder = TextDecoder;
