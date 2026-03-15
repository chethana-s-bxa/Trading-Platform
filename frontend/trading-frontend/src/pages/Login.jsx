import { useState } from "react";
import { loginUser } from "../services/authService";

function Login() {

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const handleLogin = async (e) => {

    e.preventDefault();

    try {

      const response = await loginUser({
        email,
        password
      });

      console.log("Login Response:", response);

      localStorage.setItem("token", response.token);
      localStorage.setItem("username", response.username);
      localStorage.setItem("userId", response.id);

      alert("Login successful");

    } catch (error) {

      console.error("Login failed", error);
      alert("Invalid credentials");

    }

  };

  return (

    <div>

      <h1>Login</h1>

      <form onSubmit={handleLogin}>

        <div>
          <label>Email</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>

        <br/>

        <div>
          <label>Password</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>

        <br/>

        <button type="submit">Login</button>

      </form>

    </div>

  );
}

export default Login;