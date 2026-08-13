import { ChatMessage, DeployResponse, FileNode, LoginCredentials, LoginResponse, ProjectSummaryResponse, ProjectRequest, ProjectResponse, ProjectMember, ProjectRole, MemberResponseDto, SignupRequest, AuthResponse, UserProfileResponse, PlanResponse, SubscriptionResponse, CheckoutResponse, PortalResponse } from "./types";
export type { FileNode } from "./types";

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || "").replace(/\/$/, "");
const ACCOUNT_BASE_URL = (import.meta.env.VITE_ACCOUNT_API_BASE_URL || "http://localhost:9050/account").replace(/\/$/, "");

const buildApiUrl = (path: string) => `${API_BASE_URL}${path}`;
const buildAccountUrl = (path: string) => `${ACCOUNT_BASE_URL}${path}`;

export const getAuthToken = () => localStorage.getItem("auth_token");

export const setAuthToken = (token: string) => localStorage.setItem("auth_token", token);

export const removeAuthToken = () => localStorage.removeItem("auth_token");

export const isAuthenticated = () => !!getAuthToken();

const getAuthHeaders = (): HeadersInit => {
  const token = getAuthToken();
  return token ? { Authorization: `Bearer ${token}` } : {};
};

// User info storage
export const setUserInfo = (user: { id?: number; username: string; name: string }) => {
  if (!user) return;
  localStorage.setItem("user_info", JSON.stringify(user));
};

export const getUserInfo = (): { id?: number; username: string; name: string } | null => {
  const userInfo = localStorage.getItem("user_info");
  return userInfo ? JSON.parse(userInfo) : null;
};

export const removeUserInfo = () => localStorage.removeItem("user_info");

// LocalStorage keys
export const PREVIEW_URL_KEY = "preview_url";
export const OPEN_TABS_KEY = "open_tabs";
export const ACTIVE_TAB_KEY = "active_tab";

// API response format for files endpoint
interface FilesApiResponse {
  files: { path: string }[];
}

interface FileContentApiResponse {
  content?: string;
  fileContent?: string;
  text?: string;
  data?: {
    content?: string;
    fileContent?: string;
    text?: string;
  };
}

// Convert flat file paths to nested tree structure
export function buildFileTree(paths: { path: string }[]): FileNode[] {
  const root: FileNode[] = [];
  const nodeMap = new Map<string, FileNode>();

  const sortedPaths = [...paths].sort((a, b) => a.path.localeCompare(b.path));

  for (const { path } of sortedPaths) {
    const parts = path.split("/");
    let currentPath = "";

    for (const [i, part] of parts.entries()) {
      const parentPath = currentPath;
      currentPath = currentPath ? `${currentPath}/${part}` : part;

      if (nodeMap.has(currentPath)) continue;

      const isFile = i === parts.length - 1;
      const node: FileNode = {
        name: part,
        path: currentPath,
        type: isFile ? "file" : "directory",
        children: isFile ? undefined : [],
      };

      nodeMap.set(currentPath, node);

      if (parentPath) {
        const parent = nodeMap.get(parentPath);
        if (parent && parent.children) {
          parent.children.push(node);
        }
      } else {
        root.push(node);
      }
    }
  }

  const sortNodes = (nodes: FileNode[]) => {
    nodes.sort((a, b) => {
      if (a.type === "directory" && b.type === "file") return -1;
      if (a.type === "file" && b.type === "directory") return 1;
      return a.name.localeCompare(b.name);
    });
    nodes.forEach((node) => {
      if (node.children) sortNodes(node.children);
    });
  };

  sortNodes(root);
  return root;
}

export const api = {
  async login(credentials: LoginCredentials): Promise<AuthResponse> {
    try {
      const response = await fetch(buildAccountUrl("/auth/login"), {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(credentials),
      });

      if (response.ok) {
        const data: AuthResponse = await response.json();
        const userProf = data.userProfileResponse || data.user;
        if (userProf) {
          setUserInfo(userProf);
        } else {
          setUserInfo({ username: credentials.username, name: credentials.username.split("@")[0] });
        }
        return data;
      }
    } catch {
      // Fallthrough to alternative path
    }

    const response = await fetch(buildApiUrl("/api/v1/account/auth/login"), {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(credentials),
    });

    if (!response.ok) {
      const error = await response.text();
      throw new Error(error || "Login failed");
    }

    const data: AuthResponse = await response.json();
    const userProf = data.userProfileResponse || data.user;
    if (userProf) {
      setUserInfo(userProf);
    } else {
      setUserInfo({ username: credentials.username, name: credentials.username.split("@")[0] });
    }
    return data;
  },

  async signup(data: SignupRequest): Promise<AuthResponse> {
    try {
      const response = await fetch(buildAccountUrl("/auth/signup"), {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
      });

      if (response.ok) {
        const resData: AuthResponse = await response.json();
        const userProf = resData.userProfileResponse || resData.user;
        if (userProf) {
          setUserInfo(userProf);
        } else {
          setUserInfo({ username: data.username, name: data.name || data.username.split("@")[0] });
        }
        return resData;
      }
    } catch {
      // Fallthrough
    }

    const response = await fetch(buildApiUrl("/api/v1/account/auth/signup"), {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      const error = await response.text();
      throw new Error(error || "Signup failed");
    }

    const resData: AuthResponse = await response.json();
    const userProf = resData.userProfileResponse || resData.user;
    if (userProf) {
      setUserInfo(userProf);
    } else {
      setUserInfo({ username: data.username, name: data.name || data.username.split("@")[0] });
    }
    return resData;
  },

  async getCurrentProfile(): Promise<UserProfileResponse> {
    const endpoints = [
      buildAccountUrl("/auth/profile"),
      buildAccountUrl("/profile"),
      buildApiUrl("/auth/profile"),
      buildApiUrl("/profile"),
      buildApiUrl("/api/v1/account/auth/profile"),
    ];

    for (const url of endpoints) {
      try {
        const response = await fetch(url, {
          headers: { ...getAuthHeaders() },
        });

        if (response.ok) {
          const res = await response.json();
          const raw = res.userProfileResponse || res.data || res.user || res.profile || res;
          const username = raw.username || raw.email || "";
          const name = raw.name || raw.displayName || raw.fullName || (username ? username.split("@")[0] : "");

          if (username || name) {
            const profile: UserProfileResponse = { username, name };
            setUserInfo(profile);
            return profile;
          }
        }
      } catch {
        // Try next endpoint candidate
      }
    }

    const userInfo = getUserInfo();
    return {
      username: userInfo?.username || "",
      name: userInfo?.name || (userInfo?.username ? userInfo.username.split("@")[0] : ""),
    };
  },

  async getMySubscription(): Promise<SubscriptionResponse | null> {
    try {
      const response = await fetch(buildAccountUrl("/api/subscription"), {
        headers: { ...getAuthHeaders() },
      });

      if (response.ok) {
        return response.json();
      }
    } catch {
      // Fallthrough
    }

    return null;
  },

  async getPlans(): Promise<PlanResponse[]> {
    try {
      const response = await fetch(buildAccountUrl("/api/plan"), {
        headers: { ...getAuthHeaders() },
      });

      if (response.ok) {
        return response.json();
      }
    } catch {
      // Fallthrough
    }

    return [];
  },

  async createCheckoutSession(planId: number): Promise<CheckoutResponse> {
    const response = await fetch(buildAccountUrl("/payment/checkout"), {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        ...getAuthHeaders(),
      },
      body: JSON.stringify({ planId }),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(errorText || "Failed to create checkout session");
    }

    return response.json();
  },

  async openCustomerPortal(): Promise<PortalResponse> {
    const response = await fetch(buildAccountUrl("/payment/portal"), {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        ...getAuthHeaders(),
      },
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(errorText || "Failed to open customer portal");
    }

    return response.json();
  },

  async getFiles(projectId: string): Promise<FileNode[]> {
    const response = await fetch(buildApiUrl(`/api/v1/workspace/projects/${projectId}/files/tree`), {
      headers: { ...getAuthHeaders() },
    });

    if (!response.ok) {
      throw new Error("Failed to fetch files");
    }

    const data: FilesApiResponse = await response.json();
    return buildFileTree(data.files);
  },

  async getFileContent(projectId: string, path: string): Promise<string> {
    const response = await fetch(
      buildApiUrl(`/api/v1/workspace/projects/${projectId}/files/content?path=${encodeURIComponent(path)}`),
      {
        headers: { ...getAuthHeaders() },
      }
    );

    const contentType = response.headers.get("content-type") || "";
    const rawBody = await response.text();

    if (!response.ok) {
      console.error(`Error fetching file: ${response.status} ${response.statusText}`);
      throw new Error("Failed to fetch file content");
    }

    if (!contentType.includes("application/json")) {
      return rawBody;
    }

    let data: FileContentApiResponse | string;
    try {
      data = JSON.parse(rawBody);
    } catch {
      // Some backends may return text with a JSON content-type header.
      return rawBody;
    }

    if (typeof data === "string") {
      return data;
    }

    const content =
      data.content ??
      data.fileContent ??
      data.text ??
      data.data?.content ??
      data.data?.fileContent ??
      data.data?.text;

    if (typeof content === "string") {
      return content;
    }

    console.warn("Unexpected file content response shape", data);
    return rawBody;
  },

  async deploy(projectId: string): Promise<DeployResponse> {
    const response = await fetch(buildApiUrl(`/api/v1/workspace/projects/${projectId}/deploy`), {
      method: "POST",
      headers: { ...getAuthHeaders() },
    });

    if (!response.ok) {
      throw new Error("Deployment failed");
    }

    return response.json();
  },

  async getProjects(): Promise<ProjectSummaryResponse[]> {
    const response = await fetch(buildApiUrl("/api/v1/workspace/projects"), {
      headers: { ...getAuthHeaders() },
    });

    if (!response.ok) {
      throw new Error("Failed to fetch projects");
    }

    return response.json();
  },

  async createProject(name: string): Promise<ProjectSummaryResponse> {
    const response = await fetch(buildApiUrl("/api/v1/workspace/projects"), {
      method: "POST",
      headers: { "Content-Type": "application/json", ...getAuthHeaders() },
      body: JSON.stringify({ name }),
    });

    if (!response.ok) {
      throw new Error("Failed to create project");
    }

    return response.json();
  },

  async getProject(id: string): Promise<ProjectResponse> {
    const response = await fetch(buildApiUrl(`/api/v1/workspace/projects/${id}`), {
      headers: { ...getAuthHeaders() },
    });

    if (!response.ok) {
      throw new Error("Failed to fetch project");
    }

    return response.json();
  },

  async updateProject(id: string, name: string): Promise<ProjectResponse> {
    const response = await fetch(buildApiUrl(`/api/v1/workspace/projects/${id}`), {
      method: "PATCH",
      headers: { "Content-Type": "application/json", ...getAuthHeaders() },
      body: JSON.stringify({ name }),
    });

    if (!response.ok) {
      throw new Error("Failed to update project");
    }

    return response.json();
  },

  async deleteProject(id: string): Promise<void> {
    const response = await fetch(buildApiUrl(`/api/v1/workspace/projects/${id}`), {
      method: "DELETE",
      headers: { ...getAuthHeaders() },
    });

    if (!response.ok) {
      throw new Error("Failed to delete project");
    }
  },

  async downloadProjectZip(id: string): Promise<Blob> {
    const response = await fetch(buildApiUrl(`/api/v1/workspace/projects/${id}/files/download-zip`), {
      headers: { ...getAuthHeaders() },
    });

    if (!response.ok) {
      throw new Error("Failed to download project");
    }

    return response.blob();
  },

  async getProjectMembers(projectId: string): Promise<ProjectMember[]> {
    const response = await fetch(buildApiUrl(`/api/v1/workspace/projects/${projectId}/members`), {
      headers: { ...getAuthHeaders() },
    });

    if (!response.ok) {
      throw new Error("Failed to fetch project members");
    }

    const data: MemberResponseDto[] = await response.json();
    return data.map((m) => ({
      userId: m.userId,
      username: m.username,
      name: m.name,
      role: m.projectRole,
      invitedAt: m.invitedAt,
    }));
  },

  async inviteMember(projectId: string, username: string, role: ProjectRole): Promise<void> {
    const response = await fetch(buildApiUrl(`/api/v1/workspace/projects/${projectId}/members`), {
      method: "POST",
      headers: { "Content-Type": "application/json", ...getAuthHeaders() },
      body: JSON.stringify({ userName: username, projectRole: role }),
    });

    if (!response.ok) {
      const error = await response.text();
      throw new Error(error || "Failed to invite member");
    }
  },

  async updateMemberRole(projectId: string, userId: number, role: ProjectRole): Promise<void> {
    const response = await fetch(buildApiUrl(`/api/v1/workspace/projects/${projectId}/members/${userId}`), {
      method: "PATCH",
      headers: { "Content-Type": "application/json", ...getAuthHeaders() },
      body: JSON.stringify({ projectRole: role }),
    });

    if (!response.ok) {
      throw new Error("Failed to update member role");
    }
  },

  async removeMember(projectId: string, userId: number): Promise<void> {
    const response = await fetch(buildApiUrl(`/api/v1/workspace/projects/${projectId}/members/${userId}`), {
      method: "DELETE",
      headers: { ...getAuthHeaders() },
    });

    if (!response.ok) {
      throw new Error("Failed to remove member");
    }
  },

  async getChatHistory(projectId: string): Promise<ChatMessage[]> {
    const response = await fetch(buildApiUrl(`/api/v1/intelligence/chat/projects/${projectId}`), {
      headers: { ...getAuthHeaders() },
    });

    if (!response.ok) {
      throw new Error("Failed to fetch chat history");
    }

    return response.json();
  },

  async streamChat(
    projectId: string,
    message: string,
    onChunk: (chunk: string) => void,
    onFile: (path: string, content: string) => void,
    onComplete: () => void,
    onError: (error: Error) => void
  ) {
    const controller = new AbortController();

    fetch(buildApiUrl("/api/v1/intelligence/chat/stream"), {
      method: "POST",
      headers: { "Content-Type": "application/json", ...getAuthHeaders() },
      body: JSON.stringify({ message, projectId }),
      signal: controller.signal,
    })
      .then(async (response) => {
        if (!response.ok) throw new Error("Chat stream failed");

        const reader = response.body?.getReader();
        if (!reader) throw new Error("No reader available");

        const decoder = new TextDecoder();

        // Buffers
        let sseBuffer = ""; // To handle split SSE lines
        let fullContentBuffer = ""; // To accumulate clean text for file regex
        const emittedFiles = new Map<string, string>(); // Dedupe: only re-emit a file when its content changes

        // Matches fully closed <file path="...">content</file> tags emitted by the LLM
        const FILE_TAG_REGEX = /<file path="([^"]+)">([\s\S]*?)<\/file>/g;

        const extractFiles = () => {
          FILE_TAG_REGEX.lastIndex = 0;
          let match: RegExpExecArray | null;
          while ((match = FILE_TAG_REGEX.exec(fullContentBuffer)) !== null) {
            const [, path, content] = match;
            const trimmedContent = content.trim();
            if (emittedFiles.get(path) === trimmedContent) continue;
            emittedFiles.set(path, trimmedContent);
            onFile(path, trimmedContent);
          }
        };

        while (true) {
          const { done, value } = await reader.read();
          if (done) break;

          const chunk = decoder.decode(value, { stream: true });
          sseBuffer += chunk;

          // Process line by line to handle SSE format (data: ...)
          const lines = sseBuffer.split("\n");
          sseBuffer = lines.pop() || "";

          for (const line of lines) {
            const trimmedLine = line.trim();
            if (!trimmedLine || !trimmedLine.startsWith("data:")) continue;

            const dataStr = trimmedLine.slice(5).trim();
            if (!dataStr) continue;

            try {
              // Parse JSON to get the real text with newlines preserved
              const parsed = JSON.parse(dataStr);
              const content = parsed.text;

              // 1. Send clean text to UI
              onChunk(content);

              // 2. Accumulate for file parsing and extract any newly-completed <file> tags
              fullContentBuffer += content;
              extractFiles();
            } catch (e) {
              console.error("Failed to parse SSE JSON:", e);
            }
          }
        }

        // Final pass in case the last chunk closed a file tag without a trailing chunk
        extractFiles();
        onComplete();
      })
      .catch((error) => {
        if (error.name !== "AbortError") {
          console.error("Stream error:", error);
          onError(error);
        }
      });

    return () => controller.abort();
  }

};
