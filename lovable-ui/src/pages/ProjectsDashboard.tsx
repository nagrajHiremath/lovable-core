import { useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { Plus, LogOut, Search, Folder, Loader2, MoreVertical, Trash, Download, Edit, Sparkles, FolderPlus, ArrowUpRight, Clock, LayoutGrid, CreditCard, Zap, ShieldCheck } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";
import { api, removeAuthToken, removeUserInfo, getUserInfo } from "@/lib/api";
import { ProjectSummaryResponse, SubscriptionResponse, UserProfileResponse } from "@/lib/types";
import { useToast } from "@/hooks/use-toast";
import { generateGradient, cn } from "@/lib/utils";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Logo } from "@/components/Logo";
import { UserDashboardModal } from "@/components/UserDashboardModal";
import { Badge } from "@/components/ui/badge";

export function ProjectsDashboard() {
    const navigate = useNavigate();
    const location = useLocation();
    const { toast } = useToast();
    const [projects, setProjects] = useState<ProjectSummaryResponse[]>([]);
    const [subscription, setSubscription] = useState<SubscriptionResponse | null>(null);
    const [userProfile, setUserProfile] = useState<UserProfileResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [searchQuery, setSearchQuery] = useState("");
    const [isCreating, setIsCreating] = useState(false);
    const [newProjectName, setNewProjectName] = useState("");
    const [isDialogOpen, setIsDialogOpen] = useState(false);
    const [isUserDashboardOpen, setIsUserDashboardOpen] = useState(false);

    // Rename state
    const [isRenameDialogOpen, setIsRenameDialogOpen] = useState(false);
    const [projectToRename, setProjectToRename] = useState<ProjectSummaryResponse | null>(null);
    const [renameName, setRenameName] = useState("");

    useEffect(() => {
        fetchProjectsAndSubscription();
    }, []);

    // Handle the redirect back from Stripe Checkout
    useEffect(() => {
        const params = new URLSearchParams(location.search);
        const checkout = params.get("checkout");
        if (!checkout) return;

        if (checkout === "success") {
            toast({ title: "Subscription activated", description: "Your plan has been updated." });
            fetchProjectsAndSubscription();
        } else if (checkout === "cancelled") {
            toast({ title: "Checkout cancelled", description: "No changes were made to your subscription." });
        }

        navigate(location.pathname, { replace: true });
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [location.search]);

    const fetchProjectsAndSubscription = async () => {
        try {
            const [projData, subData, profData] = await Promise.all([
                api.getProjects(),
                api.getMySubscription(),
                api.getCurrentProfile(),
            ]);
            setProjects(projData);
            setSubscription(subData);
            setUserProfile(profData);
        } catch (error) {
            console.error("Failed to fetch dashboard data:", error);
            toast({
                title: "Error",
                description: "Failed to load projects. Please try again.",
                variant: "destructive",
            });
        } finally {
            setLoading(false);
        }
    };

    const handleCreateProject = async () => {
        if (!newProjectName.trim()) return;

        setIsCreating(true);
        try {
            const newProject = await api.createProject(newProjectName);
            setProjects([newProject, ...projects]);
            setNewProjectName("");
            setIsDialogOpen(false);
            toast({
                title: "Success",
                description: "Project created successfully",
            });
        } catch (error) {
            console.error("Failed to create project:", error);
            toast({
                title: "Error",
                description: "Failed to create project",
                variant: "destructive",
            });
        } finally {
            setIsCreating(false);
        }
    };

    const handleDeleteProject = async (e: React.MouseEvent, projectId: number) => {
        e.stopPropagation();
        if (!confirm("Are you sure you want to delete this project? This action cannot be undone.")) return;

        try {
            await api.deleteProject(projectId.toString());
            setProjects(projects.filter(p => p.id !== projectId));
            toast({ title: "Success", description: "Project deleted successfully" });
        } catch (error) {
            console.error("Failed to delete:", error);
            toast({ title: "Error", description: "Failed to delete project", variant: "destructive" });
        }
    };

    const handleDownloadProject = async (e: React.MouseEvent, projectId: number) => {
        e.stopPropagation();
        try {
            const blob = await api.downloadProjectZip(projectId.toString());
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `project-${projectId}.zip`;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);
            toast({ title: "Success", description: "Download started" });
        } catch (error) {
            console.error("Failed to download:", error);
            toast({ title: "Error", description: "Failed to download project", variant: "destructive" });
        }
    };

    const handleRenameClick = (e: React.MouseEvent, project: ProjectSummaryResponse) => {
        e.stopPropagation();
        setProjectToRename(project);
        setRenameName(project.name);
        setIsRenameDialogOpen(true);
    };

    const handleRenameSubmit = async () => {
        if (!projectToRename || !renameName.trim()) return;

        try {
            await api.updateProject(projectToRename.id.toString(), renameName);
            setProjects(projects.map(p => p.id === projectToRename.id ? { ...p, name: renameName } : p));
            setIsRenameDialogOpen(false);
            setProjectToRename(null);
            toast({ title: "Success", description: "Project renamed successfully" });
        } catch (error) {
            console.error("Failed to rename:", error);
            toast({ title: "Error", description: "Failed to rename project", variant: "destructive" });
        }
    };

    const handleLogout = () => {
        removeAuthToken();
        removeUserInfo();
        navigate("/");
    };

    const currentUser = getUserInfo();
    const userName = userProfile?.name || currentUser?.name || userProfile?.username || currentUser?.username || "";
    const userEmail = userProfile?.username || currentUser?.username || "";
    const displayTitle = userName || userEmail || "Account";
    const avatarInitial = displayTitle.charAt(0).toUpperCase();
    const activePlanName = subscription?.plan?.name || "Free Plan";

    const filteredProjects = projects.filter((project) =>
        project.name.toLowerCase().includes(searchQuery.toLowerCase())
    );

    return (
        <div className="relative min-h-screen overflow-hidden bg-[#03050d] text-foreground">
            {/* User Dashboard & Subscription Modal */}
            <UserDashboardModal 
                open={isUserDashboardOpen} 
                onOpenChange={(open) => {
                    setIsUserDashboardOpen(open);
                    if (!open) {
                        fetchProjectsAndSubscription();
                    }
                }} 
            />

            {/* Background Gradient Layers with Soft, Gentle Movement */}
            <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_20%_20%,rgba(59,130,246,0.35),transparent_45%),radial-gradient(circle_at_80%_15%,rgba(168,85,247,0.32),transparent_42%),radial-gradient(circle_at_75%_75%,rgba(236,72,153,0.28),transparent_42%)] animate-gradient-pan [animation-duration:22s] opacity-75" />
            <div className="pointer-events-none absolute inset-0 bg-[linear-gradient(120deg,rgba(59,130,246,0.12)_0%,rgba(168,85,247,0.15)_35%,rgba(236,72,153,0.14)_65%,rgba(14,165,233,0.1)_100%)] bg-[length:260%_260%] animate-galaxy-flow [animation-duration:20s] mix-blend-screen opacity-70" />
            <div className="pointer-events-none absolute inset-[-25%] bg-[conic-gradient(from_180deg_at_50%_50%,rgba(59,130,246,0.14),rgba(168,85,247,0.1),rgba(236,72,153,0.14),rgba(14,165,233,0.1))] blur-[100px] animate-galaxy-spin [animation-duration:35s] mix-blend-screen opacity-65" />

            {/* Subtle Floating Color Blobs */}
            <div className="pointer-events-none absolute -left-20 top-4 h-[32rem] w-[32rem] rounded-full bg-cyan-400/25 blur-[120px] animate-blob-drift [animation-duration:24s]" />
            <div className="pointer-events-none absolute right-[-4rem] top-10 h-[36rem] w-[36rem] rounded-full bg-fuchsia-500/20 blur-[130px] animate-blob-drift [animation-duration:26s] [animation-delay:-4s]" />

            {/* Seamless Top Header - Logo on far left, Search Bar at top center, Profile on far right */}
            <header className="relative z-30 w-full pt-4 sm:pt-6">
                <div className="flex h-14 w-full items-center justify-between gap-4 px-6 lg:px-10">
                    <div className="flex items-center cursor-pointer transition hover:opacity-85 shrink-0" onClick={() => navigate("/")}>
                        <Logo hideText />
                    </div>

                    {/* Top Search Project Bar */}
                    <div className="relative w-full max-w-md">
                        <Search className="absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                        <Input
                            placeholder="Search projects..."
                            className="h-10 w-full rounded-full border border-white/10 bg-white/5 pl-10 text-sm text-white placeholder:text-slate-400 focus:border-primary/60 backdrop-blur-md shadow-inner transition"
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                        />
                    </div>

                    <div className="flex items-center gap-3 shrink-0">
                        {/* Plan Badge */}
                        <Button
                            variant="ghost"
                            onClick={() => setIsUserDashboardOpen(true)}
                            className="hidden sm:inline-flex items-center gap-1.5 rounded-full border border-pink-500/30 bg-pink-500/10 px-3.5 py-1 text-xs font-semibold text-pink-300 hover:bg-pink-500/20 transition"
                        >
                            <Zap className="h-3.5 w-3.5 text-amber-400 fill-amber-400" />
                            <span>{activePlanName}</span>
                        </Button>

                        <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                                <Button variant="ghost" size="icon" className="h-10 w-10 rounded-full border border-white/10 bg-white/5 hover:bg-white/10 transition shadow-sm">
                                    <Avatar className="h-9 w-9">
                                        <AvatarFallback className="bg-gradient-to-br from-[#FF6A5C] via-[#E056A7] to-[#7C4DFF] text-white font-bold text-sm">
                                            {avatarInitial}
                                        </AvatarFallback>
                                    </Avatar>
                                </Button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent align="end" className="w-64 border-white/10 bg-slate-950/95 backdrop-blur-xl p-2 text-foreground">
                                <div className="flex flex-col space-y-1 p-2 border-b border-white/10 mb-1">
                                    <div className="flex items-center justify-between">
                                        <p className="text-sm font-semibold text-foreground">{displayTitle}</p>
                                        <Badge className="bg-pink-500/20 text-pink-300 text-[10px] border-pink-500/30 font-bold px-1.5 py-0">
                                            {activePlanName}
                                        </Badge>
                                    </div>
                                    <p className="text-xs text-muted-foreground truncate">{userEmail}</p>
                                </div>
                                <DropdownMenuItem onClick={() => setIsUserDashboardOpen(true)} className="cursor-pointer rounded-lg font-medium text-slate-200 focus:bg-white/10">
                                    <CreditCard className="w-4 h-4 mr-2 text-pink-400" />
                                    User Dashboard & Subscription
                                </DropdownMenuItem>
                                <DropdownMenuItem onClick={handleLogout} className="text-rose-400 focus:text-rose-300 focus:bg-rose-500/10 cursor-pointer rounded-lg">
                                    <LogOut className="w-4 h-4 mr-2" />
                                    Log Out
                                </DropdownMenuItem>
                            </DropdownMenuContent>
                        </DropdownMenu>
                    </div>
                </div>
            </header>

            {/* Main Content Area */}
            <main className="relative z-10 mx-auto max-w-7xl px-6 py-8 sm:py-10 lg:px-8">
                {/* Hero Title Section */}
                <div className="mb-8 flex flex-col gap-6 lg:flex-row lg:items-end lg:justify-between">
                    <div className="space-y-3">
                        <div className="inline-flex items-center gap-2 rounded-full border border-white/10 bg-white/5 px-3 py-1 text-xs font-medium text-slate-300 backdrop-blur-md">
                            <Sparkles className="h-3.5 w-3.5 text-pink-400" />
                            <span>AI Creation Workspace</span>
                        </div>
                        <h1 className="bg-gradient-to-r from-[#FF6A5C] via-[#E056A7] to-[#7C4DFF] bg-clip-text text-3xl font-bold tracking-tight text-transparent sm:text-5xl">
                            Projects Workspace
                        </h1>
                        <p className="max-w-2xl text-sm leading-6 text-slate-300 sm:text-base">
                            Welcome back, <span className="font-semibold text-white">{userName}</span>! Manage, iterate, and build your AI-powered web applications.
                        </p>
                    </div>

                    <div className="flex items-center gap-4">
                        <div className="inline-flex items-center gap-2 rounded-xl border border-white/10 bg-slate-950/60 px-4 py-2 text-xs font-medium text-slate-300 backdrop-blur-xl">
                            <LayoutGrid className="h-4 w-4 text-pink-400" />
                            <span><strong className="text-white">{filteredProjects.length}</strong> {filteredProjects.length === 1 ? 'project' : 'projects'}</span>
                        </div>

                        {/* New Project Dialog Button */}
                        <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
                            <DialogTrigger asChild>
                                <Button className="flex items-center gap-2 rounded-xl bg-gradient-to-r from-[#FF6A5C] via-[#E056A7] to-[#7C4DFF] px-6 py-3 text-sm font-semibold text-white shadow-[0_0_25px_rgba(224,86,167,0.35)] transition-all duration-300 hover:opacity-95 hover:shadow-[0_0_35px_rgba(224,86,167,0.5)]">
                                    <Plus className="h-4 w-4" />
                                    Create New Project
                                </Button>
                            </DialogTrigger>
                            <DialogContent className="border-white/10 bg-slate-950/95 text-foreground backdrop-blur-xl max-w-md">
                                <DialogHeader>
                                    <DialogTitle className="text-xl font-bold">Create New Project</DialogTitle>
                                    <DialogDescription className="text-slate-400">
                                        Give your new workspace project a descriptive name to get started.
                                    </DialogDescription>
                                </DialogHeader>
                                <div className="py-4">
                                    <Input
                                        placeholder="e.g., E-commerce Dashboard AI"
                                        value={newProjectName}
                                        onChange={(e) => setNewProjectName(e.target.value)}
                                        onKeyDown={(e) => e.key === "Enter" && handleCreateProject()}
                                        className="h-11 rounded-xl border-white/15 bg-muted/40 text-foreground placeholder:text-slate-500 focus:border-primary"
                                    />
                                </div>
                                <DialogFooter className="gap-2 sm:gap-0">
                                    <Button variant="outline" onClick={() => setIsDialogOpen(false)} className="rounded-xl border-white/10 bg-transparent text-slate-300 hover:bg-white/5">
                                        Cancel
                                    </Button>
                                    <Button onClick={handleCreateProject} disabled={isCreating || !newProjectName.trim()} className="rounded-xl bg-gradient-to-r from-[#FF6A5C] via-[#E056A7] to-[#7C4DFF] text-white">
                                        {isCreating && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                                        Create Project
                                    </Button>
                                </DialogFooter>
                            </DialogContent>
                        </Dialog>
                    </div>

                    {/* Rename Project Dialog */}
                    <Dialog open={isRenameDialogOpen} onOpenChange={setIsRenameDialogOpen}>
                        <DialogContent className="border-white/10 bg-slate-950/95 text-foreground backdrop-blur-xl max-w-md">
                            <DialogHeader>
                                <DialogTitle className="text-xl font-bold">Rename Project</DialogTitle>
                            </DialogHeader>
                            <div className="py-4">
                                <Input
                                    value={renameName}
                                    onChange={(e) => setRenameName(e.target.value)}
                                    onKeyDown={(e) => e.key === "Enter" && handleRenameSubmit()}
                                    className="h-11 rounded-xl border-white/15 bg-muted/40 text-foreground focus:border-primary"
                                />
                            </div>
                            <DialogFooter className="gap-2 sm:gap-0">
                                <Button variant="outline" onClick={() => setIsRenameDialogOpen(false)} className="rounded-xl border-white/10 bg-transparent text-slate-300 hover:bg-white/5">
                                    Cancel
                                </Button>
                                <Button onClick={handleRenameSubmit} disabled={!renameName.trim() || renameName === projectToRename?.name} className="rounded-xl bg-gradient-to-r from-[#FF6A5C] via-[#E056A7] to-[#7C4DFF] text-white">
                                    Save Changes
                                </Button>
                            </DialogFooter>
                        </DialogContent>
                    </Dialog>
                </div>

                {/* Grid */}
                {loading ? (
                    <div className="flex flex-col items-center justify-center py-24 gap-3">
                        <Loader2 className="h-10 w-10 animate-spin text-pink-500" />
                        <p className="text-sm text-slate-400 animate-pulse">Loading your workspaces...</p>
                    </div>
                ) : filteredProjects.length === 0 ? (
                    <div className="flex flex-col items-center justify-center rounded-3xl border border-dashed border-white/15 bg-slate-950/40 p-12 text-center backdrop-blur-xl">
                        <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-white/5 border border-white/10">
                            <FolderPlus className="h-8 w-8 text-pink-400" />
                        </div>
                        <h3 className="text-xl font-bold text-white mb-2">No projects found</h3>
                        <p className="max-w-md text-sm text-slate-400 mb-6">
                            {searchQuery ? `No matching results found for "${searchQuery}". Try searching with a different term.` : "You don't have any active projects yet. Create your first workspace to start building."}
                        </p>
                        {!searchQuery && (
                            <Button onClick={() => setIsDialogOpen(true)} className="rounded-xl bg-gradient-to-r from-[#FF6A5C] via-[#E056A7] to-[#7C4DFF] px-6 text-white font-medium">
                                <Plus className="mr-2 h-4 w-4" />
                                Create Your First Project
                            </Button>
                        )}
                    </div>
                ) : (
                    <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
                        {filteredProjects.map((project) => (
                            <Card
                                key={project.id}
                                className="group relative cursor-pointer overflow-hidden rounded-2xl border border-white/10 bg-slate-950/70 backdrop-blur-xl transition-all duration-300 hover:-translate-y-1 hover:border-white/25 hover:shadow-[0_20px_40px_rgba(124,77,255,0.22)] flex flex-col justify-between"
                                onClick={() => navigate(`/projects/${project.id}`)}
                            >
                                <div>
                                    <CardHeader className="p-0">
                                        <div className="relative aspect-video w-full overflow-hidden rounded-t-2xl bg-slate-900">
                                            {project.thumbnailUrl ? (
                                                <img
                                                    src={project.thumbnailUrl}
                                                    alt={project.name}
                                                    className="h-full w-full object-cover transition-transform duration-500 group-hover:scale-105"
                                                />
                                            ) : (
                                                <div
                                                    className="h-full w-full transition-opacity group-hover:opacity-90"
                                                    style={generateGradient(project.name)}
                                                />
                                            )}
                                            <div className="absolute inset-0 bg-gradient-to-t from-slate-950 via-transparent to-transparent opacity-60" />
                                            {project.role && (
                                                <div className="absolute top-3 left-3">
                                                    <span className={cn(
                                                        "text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-full border backdrop-blur-md shadow-sm",
                                                        project.role === 'OWNER' ? "bg-pink-500/20 text-pink-300 border-pink-500/30" :
                                                            project.role === 'DEVELOPER' ? "bg-amber-500/20 text-amber-300 border-amber-500/30" :
                                                                "bg-slate-800/60 text-slate-300 border-slate-700"
                                                    )}>
                                                        {project.role}
                                                    </span>
                                                </div>
                                            )}
                                        </div>
                                    </CardHeader>
                                    <CardContent className="p-5">
                                        <div className="flex items-start justify-between gap-2">
                                            <CardTitle className="text-lg font-bold text-white transition-colors group-hover:text-pink-300 line-clamp-1">
                                                {project.name}
                                            </CardTitle>
                                            <DropdownMenu>
                                                <DropdownMenuTrigger asChild onClick={(e) => e.stopPropagation()}>
                                                    <Button variant="ghost" size="icon" className="h-8 w-8 -mt-1 -mr-2 text-slate-400 hover:bg-white/10 hover:text-white rounded-lg">
                                                        <MoreVertical className="h-4 w-4" />
                                                    </Button>
                                                </DropdownMenuTrigger>
                                                <DropdownMenuContent align="end" className="w-44 border-white/10 bg-slate-950/95 text-foreground backdrop-blur-xl p-1">
                                                    <DropdownMenuItem onClick={(e) => handleRenameClick(e, project)} className="cursor-pointer rounded-md focus:bg-white/10">
                                                        <Edit className="w-4 h-4 mr-2 text-slate-300" />
                                                        Rename
                                                    </DropdownMenuItem>
                                                    <DropdownMenuItem onClick={(e) => handleDownloadProject(e, project.id)} className="cursor-pointer rounded-md focus:bg-white/10">
                                                        <Download className="w-4 h-4 mr-2 text-slate-300" />
                                                        Download ZIP
                                                    </DropdownMenuItem>
                                                    <DropdownMenuItem className="text-rose-400 focus:text-rose-300 focus:bg-rose-500/10 cursor-pointer rounded-md" onClick={(e) => handleDeleteProject(e, project.id)}>
                                                        <Trash className="w-4 h-4 mr-2" />
                                                        Delete
                                                    </DropdownMenuItem>
                                                </DropdownMenuContent>
                                            </DropdownMenu>
                                        </div>
                                    </CardContent>
                                </div>

                                <CardFooter className="flex items-center justify-between border-t border-white/10 p-5 py-3 text-xs text-slate-400 bg-white/[0.02]">
                                    <div className="flex items-center gap-1.5">
                                        <Clock className="h-3.5 w-3.5 text-slate-400" />
                                        <span>{new Date(project.createdAt).toLocaleDateString()}</span>
                                    </div>
                                    <div className="flex items-center gap-1 font-medium text-pink-400 opacity-0 group-hover:opacity-100 transition-opacity">
                                        <span>Open</span>
                                        <ArrowUpRight className="h-3.5 w-3.5 transition-transform group-hover:translate-x-0.5 group-hover:-translate-y-0.5" />
                                    </div>
                                </CardFooter>
                            </Card>
                        ))}
                    </div>
                )}
            </main>
        </div>
    );
}
